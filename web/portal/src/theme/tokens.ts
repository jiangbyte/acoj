import type { ThemeConfig } from 'antd'

/**
 * Portal 主题：主色对齐 admin `stores/app/theme.json`（#1677FF），
 * 难度 / 判题用 Ant Design 语义色，保持校园活力、避免发灰发闷。
 */
export const portalSeedToken = {
  colorPrimary: '#1677FF',
  borderRadius: 10,
  colorBgLayout: '#f5f7fb',
  colorBorder: '#e6ebf2',
  colorBorderSecondary: '#eef2f7',
  colorSplit: '#eef2f7',
  fontFamily:
    "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif",
  zIndexPopupBase: 200000,
  colorDiffEasy: '#52c41a',
  colorDiffMedium: '#faad14',
  colorDiffHard: '#ff4d4f',
  colorVerdictAc: '#52c41a',
  colorVerdictWa: '#ff4d4f',
  colorVerdictTle: '#fa8c16',
  colorVerdictCe: '#8c8c8c',
  colorVerdictRe: '#ff4d4f',
  colorVerdictPending: '#1677FF',
  colorRankGold: '#faad14',
  colorRankSilver: '#bfbfbf',
  colorRankBronze: '#d48806',
} as const

export const portalComponentToken: ThemeConfig['components'] = {
  Table: {
    headerBg: 'transparent',
    borderColor: '#eef2f7',
  },
  Card: {
    paddingLG: 20,
  },
  Tabs: {
    horizontalItemPadding: '10px 16px',
  },
}
