export {
  fetchLogin,
  fetchCurrentUser,
  fetchUserMenus,
  fetchUserPermissions,
  fetchLogout,
} from './auth'
export {
  fetchUserPage,
  fetchUserRemove,
  fetchUserDetail,
  fetchUserCreate,
  fetchUserModify,
} from './user'
export {
  fetchRolePage,
  fetchRoleRemove,
  fetchRoleDetail,
  fetchRoleCreate,
  fetchRoleModify,
  fetchRoleOwnPermission,
  fetchRoleGrantPermission,
} from './role'
export { fetchOrgPage, fetchOrgRemove } from './org'
export { fetchGroupPage, fetchGroupRemove } from './group'
export { fetchPositionPage, fetchPositionRemove } from './position'
export { fetchNoticePage, fetchNoticeRemove } from './notice'
export { fetchBannerPage, fetchBannerRemove } from './banner'
export { fetchConfigPage, fetchConfigRemove } from './config'
export { fetchFilePage, fetchFileRemove, uploadFile } from './file'
export { fetchResourcePage, fetchResourceRemove } from './resource'
export { fetchPermissionPage, fetchPermissionRemove, fetchPermissionByModule } from './permission'
export { fetchDictTree, fetchDictPage, fetchDictRemove } from './dict'
