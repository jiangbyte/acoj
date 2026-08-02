import { Card, Col, Row, Space, Typography } from 'antd'
import { Link } from 'react-router-dom'
import { DictSelect } from '@/components/common/DictSelect'
import { DictTag } from '@/components/common/DictTag'
import { useAuthStore } from '@/stores/auth'
import { isDictLoaded } from '@/utils/dict'

const entries = [
  { title: '题库', desc: '公开练习题（下一阶段接入）', to: '/problems' },
  { title: '竞赛', desc: '竞赛列表与报名（下一阶段接入）', to: '/contests' },
  { title: '提交', desc: '提交记录（下一阶段接入）', to: '/submissions' },
  { title: '排名', desc: 'Rating 排行（下一阶段接入）', to: '/rank' },
]

export function HomePage() {
  const token = useAuthStore((s) => s.token)

  return (
    <div className="space-y-6">
      <div>
        <Typography.Title level={2} className="!mb-2">
          ACOJ
        </Typography.Title>
        <Typography.Paragraph type="secondary" className="!mb-0">
          {token
            ? '已登录。认证、请求解包与字典缓存已接入。'
            : '未登录。可前往登录页验证验证码与 RSA 登录。'}
        </Typography.Paragraph>
      </div>

      <Card size="small" title="字典联调（COMMON_STATUS）">
        <Space wrap>
          <Typography.Text type="secondary">
            已加载：{isDictLoaded() ? '是' : '加载中…'}（公开接口，无需登录）
          </Typography.Text>
          <DictSelect dictCode="COMMON_STATUS" placeholder="选择状态" className="min-w-160px" />
          <DictTag dictCode="COMMON_STATUS" value="ENABLED" />
          <DictTag dictCode="COMMON_STATUS" value="DISABLED" />
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {entries.map((item) => (
          <Col xs={24} sm={12} md={6} key={item.to}>
            <Link to={item.to} className="block no-underline">
              <Card hoverable title={item.title}>
                <Typography.Text type="secondary">{item.desc}</Typography.Text>
              </Card>
            </Link>
          </Col>
        ))}
      </Row>
    </div>
  )
}
