import { useMemo } from 'react'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { Typography } from 'antd'
import './markdown.css'

type Props = {
  content: string
  className?: string
}

export function Markdown({ content, className }: Props) {
  const components = useMemo(
    () => ({
      h1: (props: { children?: React.ReactNode }) => (
        <Typography.Title level={3} className="markdown-title">
          {props.children}
        </Typography.Title>
      ),
      h2: (props: { children?: React.ReactNode }) => (
        <Typography.Title level={4} className="markdown-title">
          {props.children}
        </Typography.Title>
      ),
      h3: (props: { children?: React.ReactNode }) => (
        <Typography.Title level={5} className="markdown-title">
          {props.children}
        </Typography.Title>
      ),
      a: (props: { href?: string; children?: React.ReactNode }) => (
        <a href={props.href} target="_blank" rel="noreferrer">
          {props.children}
        </a>
      ),
    }),
    [],
  )

  return (
    <div className={`markdown-body ${className ?? ''}`}>
      <ReactMarkdown remarkPlugins={[remarkGfm]} components={components}>
        {content}
      </ReactMarkdown>
    </div>
  )
}
