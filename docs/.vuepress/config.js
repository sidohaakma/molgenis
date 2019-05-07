module.exports = {
  title: 'MOLGENIS guide',
  description: "For scientific data",
  themeConfig: {
    lastUpdated: 'Last Updated', // string | boolean
    repo: 'molgenis/molgenis',
    docsDir: 'docs',
    editLinks: true,
    searchMaxSuggestions: 20,
    nav: [
      {
        text: 'Guide',
        link: '/background'
      },
      {
        text: 'Releases',
        link: 'https://github.com/molgenis/molgenis/releases'
      },
      {
        text: 'Website',
        link: 'http://molgenis.org'
      },

    ],
    sidebar: [
      {
        title: "Introduction",
        children: [
          'background.md',
          'guide-try-out-molgenis',
          'guide-docker'
        ]
      },
      {
        title: 'Find, view, query',
        children: [
          'guide-explore',
          'guide-navigator',
          'guide-search',
          'guide-authentication'
        ]
      },
      {
        title: 'Data management',
        children: [
          'guide-emx',
          'guide-upload',
          'guide-quick-upload',
          'guide-metadata-manager',
          'guide-questionnaire',
          'guide-emx-download',
          'guide-expressions'
        ]
      },
      {
        title: 'Access control',
        children: [
          'guide-user-management',
          'guide-groups-roles',
          'guide-permission-manager'
        ]
      },
      {
        title: 'Data processing',
        children: [
          'guide-scripts',
          'guide-r',
          'guide-python',
          'guide-schedule'
        ]
      },
      {
        title: 'Configuration',
        children: [
          'guide-settings',
          'guide-customize',
          'guide-l10n',
          'guide-app-manager',
          'guide-creating-themes'
        ]
      },
      {
        title: 'Interoperability',
        children: [
          'guide-swagger',
          'guide-api-rest',
          'guide-api-rest2',
          'guide-api-files',
          'guide-api-import',
          'guide-client-r',
          'guide-client-python',
          'guide-beacon',
          'guide-fair',
          'guide-rsql'
        ]
      },
      {
        title: 'For developers',
        children: [
          'guide-local-compile',
          'guide-intellij',
          'guide-technologies',
          'guide-app-development',
          'guide-dynamic-decorators',
          'guide-frontend-development',
          'guide-integration-tests',
          'guide-jobs',
          'guide-security'
        ]
      },
      {
        title: 'For system administrators',
        children: [
          'guide-kubernetes',
          'guide-standalone-server',
          'guide-migration'
        ]
      }
    ]
  }
}
