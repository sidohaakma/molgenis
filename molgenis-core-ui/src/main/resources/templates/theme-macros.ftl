<#-- Generates theme href by -->
<#-- * appending theme name with automatically generated version number (this fingerprint avoids the use of stale browser data)-->
<#macro theme_href theme_name><#assign filtered_theme_name = theme_name>${filtered_theme_name?html}<#if environment == "production"><#attempt>?${theme_fingerprint_registry.getFingerprint(filtered_theme_name)?html}<#recover></#attempt></#if></#macro>