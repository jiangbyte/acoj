import { Button } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useLocation, useNavigate } from 'react-router-dom'
import { Logo } from './Logo'
import { UserCenter } from './UserCenter'

const CONTEST_PROBLEM_PATTERN = /^\/contests\/([^/]+)\/problems\//

export function SolveHeader() {
  const navigate = useNavigate()
  const { pathname } = useLocation()

  const match = pathname.match(CONTEST_PROBLEM_PATTERN)
  const backTarget = match ? `/contests/${match[1]}` : '/problems'

  return (
    <header className="flex h-16 shrink-0 items-center border-b border-gray-200 bg-white">
      <div className="flex min-w-0 flex-1 items-center gap-3 px-4">
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          aria-label="返回"
          onClick={() => navigate(backTarget)}
        />
        <Logo />
      </div>
      <div className="shrink-0 px-4">
        <UserCenter />
      </div>
    </header>
  )
}
