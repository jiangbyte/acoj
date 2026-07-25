export function useModels() {
  const model = useCookie<string>('ai-model', {
    default: () => 'claude-haiku',
  })

  const models = [
    { label: 'Claude Haiku 4.5', value: 'claude-haiku', icon: 'i-lucide-sparkles' },
    { label: 'Gemini 3 Flash', value: 'gemini-flash', icon: 'i-lucide-sparkles' },
    { label: 'GPT-5 Nano', value: 'gpt-5-nano', icon: 'i-lucide-sparkles' },
  ]

  return { model, models }
}
