import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import './auth-page.css'

type SplitProps = {
  title: string
  headerExtra?: ReactNode
  children: ReactNode
}

const brandName = import.meta.env.VITE_APP_TITLE || 'ACOJ'

const features = [
  '题库练习与提交记录',
  '课程路径与今日练习',
  '竞赛报名与实时榜单',
]

/** 登录 / 注册：Gitee 风格左右分栏卡片 */
export function AuthSplit({ title, headerExtra, children }: SplitProps) {
  return (
    <div className="auth-page">
      <div className="auth-card">
        <aside className="auth-card__brand" aria-hidden={false}>
          <div className="auth-card__brand-deco" aria-hidden />
          <div className="auth-card__brand-inner">
            <Link to="/" className="auth-card__logo">
              <span className="auth-card__logo-mark">{brandName.slice(0, 1).toUpperCase()}</span>
              <span className="auth-card__logo-text">{brandName}</span>
            </Link>
            <h2 className="auth-card__headline">校园在线评测平台</h2>
            <p className="auth-card__lead">
              服务课程教学与日常练习：做题、参赛、看排名，一站完成。
            </p>
            <ul className="auth-card__features">
              {features.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
            <div className="auth-card__brand-foot">
              <Link to="/" className="auth-card__brand-link">
                进入门户首页
              </Link>
            </div>
          </div>
        </aside>

        <div className="auth-card__form">
          <div className="auth-card__form-head">
            <h1 className="auth-card__title">{title}</h1>
            {headerExtra ? <div className="auth-card__form-extra">{headerExtra}</div> : null}
          </div>
          {children}
        </div>
      </div>
    </div>
  )
}

type CenterProps = {
  title: string
  description?: string
  children: ReactNode
}

/** 找回 / 重置密码：居中简版 */
export function AuthCenter({ title, description, children }: CenterProps) {
  return (
    <div className="auth-page auth-page--center">
      <div className="auth-center">
        <Link to="/" className="auth-center__logo">
          <span className="auth-center__logo-mark">{brandName.slice(0, 1).toUpperCase()}</span>
        </Link>
        <h1 className="auth-center__title">{title}</h1>
        {description ? <p className="auth-center__desc">{description}</p> : null}
        <div className="auth-center__body">{children}</div>
      </div>
    </div>
  )
}
