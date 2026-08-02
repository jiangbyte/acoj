import { Select } from 'antd'
import type { SelectProps } from 'antd'
import { dictList } from '@/utils/dict'

type Props = Omit<SelectProps, 'options'> & {
  dictCode: string
}

export function DictSelect({ dictCode, ...rest }: Props) {
  const options = dictList(dictCode)
  return <Select allowClear options={options} {...rest} />
}
