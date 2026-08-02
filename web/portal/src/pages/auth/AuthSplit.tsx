import type { ReactNode } from 'react'
import { Card, Typography } from 'antd'
import { CodeOutlined, SafetyCertificateOutlined, TrophyOutlined } from '@ant-design/icons'
import './auth-page.css'

type Props = {
  title: string
  subtitle: string
  children: ReactNode
}

const highlights = [
  {
    icon: <SafetyCertificateOutlined />,
    title: '安全登录',
    text: '验证码与加密传输保障账号安全',
  },
  {
    icon: <CodeOutlined />,
    title: '在线评测',
    text: '题库练习与提交记录一站完成',
  },
  {
    icon: <TrophyOutlined />,
    title: '竞赛参赛',
    text: '报名参赛、查看榜单与答疑',
  },
]

export function AuthSplit({ title, subtitle, children }: Props) {
  return (
    <section className="auth-split">
      <aside className="auth-split__visual" aria-hidden>
        <div className="auth-split__visual-inner">
          <Typography.Text type="success" strong>
            ACOJ 在线评测门户
          </Typography.Text>
          <Typography.Title level={2} className="!mt-2 !mb-0">
            刷题、竞赛、排名一站完成
          </Typography.Title>
          <Typography.Paragraph type="secondary" className="!mb-0 !mt-3">
            面向选手的身份入口。登录后即可提交代码、参加竞赛并查看 Rating。
          </Typography.Paragraph>
          <div className="auth-split__highlights">
            {highlights.map((item) => (
              <Card key={item.title} size="small">
                <div className="flex gap-2">
                  <span className="text-lg" style={{ color: 'var(--ant-color-primary)' }}>
                    {item.icon}
                  </span>
                  <div>
                    <div className="font-medium">{item.title}</div>
                    <Typography.Text type="secondary" className="text-xs">
                      {item.text}
                    </Typography.Text>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </aside>

      <div className="auth-split__panel">
        <Card className="auth-split__card">
          <Typography.Title level={3} className="!mt-0 !mb-1">
            {title}
          </Typography.Title>
          <Typography.Paragraph type="secondary" className="!mb-6">
            {subtitle}
          </Typography.Paragraph>
          {children}
        </Card>
      </div>
    </section>
  )
}
