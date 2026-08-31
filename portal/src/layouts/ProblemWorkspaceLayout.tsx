/** Author: Charlie */

import { Outlet } from 'react-router-dom'

/** 做题工作台外壳：全屏固定视口，不挂站点头脚。 */
export function ProblemWorkspaceLayout() {
  return (
    <div className="h-dvh w-full overflow-hidden bg-[var(--ant-color-bg-layout)] text-[var(--ant-color-text)]">
      <Outlet />
    </div>
  )
}
