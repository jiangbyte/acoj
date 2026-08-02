import { useState } from 'react'
import type { ReactNode } from 'react'

export type CustomTabItem = {
  key: string
  label: string
  icon?: ReactNode
  children?: ReactNode
}

type Props = {
  items: CustomTabItem[]
  defaultActiveKey?: string
  activeKey?: string
  onChange?: (key: string) => void
  className?: string
  contentClassName?: string
}

export function CustomTabs({
  items,
  defaultActiveKey,
  activeKey,
  onChange,
  className,
  contentClassName,
}: Props) {
  const [innerActiveKey, setInnerActiveKey] = useState(defaultActiveKey ?? items[0]?.key)
  const current = activeKey ?? innerActiveKey
  const activeItem = items.find((item) => item.key === current) ?? items[0]

  function handleClick(key: string) {
    if (onChange) {
      onChange(key)
    } else {
      setInnerActiveKey(key)
    }
  }

  return (
    <div className={`flex h-full min-h-0 flex-col ${className ?? ''}`}>
      <div className="flex shrink-0 items-center gap-1 border-b border-gray-200">
        {items.map((item) => (
          <button
            key={item.key}
            type="button"
            className={`flex h-9 cursor-pointer items-center gap-1 border-b-2 px-4 text-sm transition-colors ${
              current === item.key
                ? 'border-blue-500 font-medium text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-800'
            }`}
            onClick={() => handleClick(item.key)}
          >
            {item.icon}
            {item.label}
          </button>
        ))}
      </div>
      <div className={`min-h-0 flex-1 overflow-y-auto ${contentClassName ?? ''}`}>
        {activeItem?.children}
      </div>
    </div>
  )
}
