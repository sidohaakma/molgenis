// @flow
import type { State } from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  message: INITIAL_STATE.message,
  rawData: {
    '__entityTypeId': 'sys_md_Package',
    'id': 'sys',
    'label': 'System',
    'description': 'Package containing all system entities',
    'children': [
      {
        '__entityTypeId': 'sys_md_Package',
        '__idValue': 'sys_idx',
        '__labelValue': 'Index'
      }, {
        '__entityTypeId': 'sys_md_Package',
        '__idValue': 'sys_job',
        '__labelValue': 'Jobs'
      }, {
        '__entityTypeId': 'sys_md_Package',
        '__idValue': 'sys_mail',
        '__labelValue': 'Mail'
      }, {
        '__entityTypeId': 'sys_md_Package',
        '__idValue': 'sys_map',
        '__labelValue': 'Mapper'
      }, {
        '__entityTypeId': 'sys_md_Package',
        '__idValue': 'sys_md',
        '__labelValue': 'Meta'
      }, {
        '__entityTypeId': 'sys_md_Package',
        '__idValue': 'sys_ont',
        '__labelValue': 'Ontology'
      }, {
        '__entityTypeId': 'sys_md_Package',
        '__idValue': 'sys_scr',
        '__labelValue': 'Script'
      }, {
        '__entityTypeId': 'sys_md_Package',
        '__idValue': 'sys_sec',
        '__labelValue': 'Security'
      }, {
        '__entityTypeId': 'sys_md_Package',
        '__idValue': 'sys_set',
        '__labelValue': 'Settings'
      }
    ],
    'entityTypes': [
      {
        '__entityTypeId': 'sys_md_EntityType',
        '__idValue': 'sys_App',
        '__labelValue': 'App'
      }, {
        '__entityTypeId': 'sys_md_EntityType',
        '__idValue': 'sys_FileMeta',
        '__labelValue': 'File metadata'
      }, {
        '__entityTypeId': 'sys_md_EntityType',
        '__idValue': 'sys_FreemarkerTemplate',
        '__labelValue': 'Freemarker template'
      }, {
        '__entityTypeId': 'sys_md_EntityType',
        '__idValue': 'sys_ImportRun',
        '__labelValue': 'Import'
      }, {
        '__entityTypeId': 'sys_md_EntityType',
        '__idValue': 'sys_Language',
        '__labelValue': 'Language'
      }, {
        '__entityTypeId': 'sys_md_EntityType',
        '__idValue': 'sys_L10nString',
        '__labelValue': 'Localization'
      }, {
        '__entityTypeId': 'sys_md_EntityType',
        '__idValue': 'sys_Questionnaire',
        '__labelValue': 'Questionnaire'
      }, {
        '__entityTypeId': 'sys_md_EntityType',
        '__idValue': 'sys_StaticContent',
        '__labelValue': 'Static content'
      }
    ],
    'tags': []
  }
}

export default state
