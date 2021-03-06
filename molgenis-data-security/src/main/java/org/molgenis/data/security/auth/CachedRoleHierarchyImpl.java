package org.molgenis.data.security.auth;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.molgenis.data.security.DataserviceRoleHierarchy;
import org.molgenis.data.transaction.TransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

public class CachedRoleHierarchyImpl implements TransactionListener, CachedRoleHierarchy {
  private final DataserviceRoleHierarchy dataserviceRoleHierarchy;
  private ImmutableMap<GrantedAuthority, ImmutableSet<GrantedAuthority>>
      cachedReachableAuthoritiesMap;
  private final ThreadLocal<Boolean> cacheDirty;

  public CachedRoleHierarchyImpl(
      DataserviceRoleHierarchy dataserviceRoleHierarchy, TransactionManager transactionManager) {
    this.dataserviceRoleHierarchy = requireNonNull(dataserviceRoleHierarchy);
    requireNonNull(transactionManager).addTransactionListener(this);
    this.cacheDirty = new ThreadLocal<>();
  }

  @Override
  public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
      Collection<? extends GrantedAuthority> authorities) {
    Collection<? extends GrantedAuthority> reachableGrantedAuthorities;

    if (authorities == null || authorities.isEmpty()) {
      reachableGrantedAuthorities = AuthorityUtils.NO_AUTHORITIES;
    } else {
      Boolean isCacheDirty = cacheDirty.get();
      if (isCacheDirty == null || !isCacheDirty) {
        reachableGrantedAuthorities = getCachedReachableAuthorities(authorities);
      } else {
        reachableGrantedAuthorities = getPersistedReachableGrantedAuthorities(authorities);
      }
    }

    return reachableGrantedAuthorities;
  }

  private Collection<GrantedAuthority> getCachedReachableAuthorities(
      Collection<? extends GrantedAuthority> authorities) {
    if (cachedReachableAuthoritiesMap == null) {
      cachedReachableAuthoritiesMap = createReachableAuthoritiesMap();
    }

    Collection<GrantedAuthority> reachableGrantedAuthorities;
    if (authorities.size() == 1) {
      GrantedAuthority grantedAuthority = authorities.iterator().next();
      reachableGrantedAuthorities = getCachedReachableAuthorities(grantedAuthority);
    } else {
      ImmutableSet.Builder<GrantedAuthority> builder = ImmutableSet.builder();
      for (GrantedAuthority authority : authorities) {
        builder.addAll(getCachedReachableAuthorities(authority));
      }
      reachableGrantedAuthorities = builder.build();
    }
    return reachableGrantedAuthorities;
  }

  private Collection<GrantedAuthority> getCachedReachableAuthorities(
      GrantedAuthority grantedAuthority) {
    Collection<GrantedAuthority> reachableGrantedAuthorities =
        cachedReachableAuthoritiesMap.get(grantedAuthority);
    if (reachableGrantedAuthorities == null) {
      return singleton(grantedAuthority);
    }
    return reachableGrantedAuthorities;
  }

  private Collection<? extends GrantedAuthority> getPersistedReachableGrantedAuthorities(
      Collection<? extends GrantedAuthority> authorities) {
    return runAsSystem(() -> dataserviceRoleHierarchy.getReachableGrantedAuthorities(authorities));
  }

  private synchronized ImmutableMap<GrantedAuthority, ImmutableSet<GrantedAuthority>>
      createReachableAuthoritiesMap() {
    ImmutableMap<GrantedAuthority, ImmutableSet<GrantedAuthority>> authorityInclusions =
        runAsSystem(dataserviceRoleHierarchy::getAllGrantedAuthorityInclusions);

    Builder<GrantedAuthority, ImmutableSet<GrantedAuthority>> builder = ImmutableMap.builder();
    authorityInclusions
        .keySet()
        .forEach(
            authority ->
                builder.put(
                    authority, this.getReachableAuthorities(authority, authorityInclusions)));

    return builder.build();
  }

  private ImmutableSet<GrantedAuthority> getReachableAuthorities(
      GrantedAuthority authority,
      ImmutableMap<GrantedAuthority, ImmutableSet<GrantedAuthority>> authorityInclusions) {
    Set<GrantedAuthority> authorities = new HashSet<>();
    getReachableAuthoritiesRecursive(authority, authorities, authorityInclusions);
    return ImmutableSet.copyOf(authorities);
  }

  /**
   * Recursively adds reachable authorities to a set and returns when an authority has already been
   * encountered, stopping the iteration over circular hierarchies.
   */
  private void getReachableAuthoritiesRecursive(
      GrantedAuthority currentAuthority,
      Set<GrantedAuthority> authorities,
      ImmutableMap<GrantedAuthority, ImmutableSet<GrantedAuthority>> authorityInclusions) {
    if (!authorities.contains(currentAuthority)) {
      authorities.add(currentAuthority);

      authorityInclusions
          .get(currentAuthority)
          .forEach(
              authorityInclude ->
                  getReachableAuthoritiesRecursive(
                      authorityInclude, authorities, authorityInclusions));
    }
  }

  /** Marks the role hierarchy cache as dirty for the current transaction/thread. */
  @Override
  public void markRoleHierarchyCacheDirty() {
    cacheDirty.set(true);
  }

  @Override
  public void transactionStarted(String transactionId) {
    cacheDirty.set(false);
  }

  @Override
  public void afterCommitTransaction(String transactionId) {
    Boolean isCacheDirty = cacheDirty.get();
    if (isCacheDirty != null && isCacheDirty) {
      cachedReachableAuthoritiesMap = null;
    }
  }

  @Override
  public void rollbackTransaction(String transactionId) {
    cacheDirty.set(false);
  }

  @Override
  public void doCleanupAfterCompletion(String transactionId) {
    cacheDirty.remove();
  }
}
