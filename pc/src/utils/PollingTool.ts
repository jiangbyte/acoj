// 轮询工具类
export class PollingTool {
  private intervalId: NodeJS.Timeout | null = null
  private isPaused: boolean = false
  private lastExecuted: Date | null = null
  private nextExecution: Date | null = null

  constructor(
    private task: () => void | Promise<void>,
    private intervalMs: number = 5000,
  ) {}

  // 启动轮询
  start(): void {
    if (this.intervalId) {
      console.warn('轮询已经在运行中')
      return
    }

    this.isPaused = false
    this.executeTask() // 立即执行一次任务

    this.intervalId = setInterval(() => {
      if (!this.isPaused) {
        this.executeTask()
      }
    }, this.intervalMs)

    this.calculateNextExecution()
    console.log('轮询已启动')
  }

  // 执行任务
  private async executeTask(): Promise<void> {
    try {
      this.lastExecuted = new Date()
      await this.task()
    }
    catch (error) {
      console.error('轮询任务执行出错:', error)
    }
    finally {
      this.calculateNextExecution()
    }
  }

  // 计算下一次执行时间
  private calculateNextExecution(): void {
    if (this.lastExecuted) {
      this.nextExecution = new Date(this.lastExecuted.getTime() + this.intervalMs)
    }
  }

  // 暂停轮询
  pause(): void {
    if (!this.intervalId) {
      console.warn('轮询未运行，无法暂停')
      return
    }

    this.isPaused = true
    console.log('轮询已暂停')
  }

  // 恢复轮询
  resume(): void {
    if (!this.intervalId) {
      console.warn('轮询未运行，无法恢复')
      return
    }

    if (!this.isPaused) {
      console.warn('轮询未暂停，无需恢复')
      return
    }

    this.isPaused = false
    // 立即执行一次任务，然后继续按间隔执行
    this.executeTask()
    console.log('轮询已恢复')
  }

  // 停止轮询
  stop(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId)
      this.intervalId = null
    }

    this.isPaused = false
    this.lastExecuted = null
    this.nextExecution = null
    console.log('轮询已停止')
  }

  // 更新轮询间隔
  updateInterval(newIntervalMs: number): void {
    if (newIntervalMs <= 0) {
      throw new Error('轮询间隔必须大于0')
    }

    const wasRunning = !!this.intervalId && !this.isPaused

    if (this.intervalId) {
      this.stop()
    }

    this.intervalMs = newIntervalMs

    if (wasRunning) {
      this.start()
    }

    console.log(`轮询间隔已更新为: ${newIntervalMs}ms`)
  }

  // 更新任务
  updateTask(newTask: () => void | Promise<void>): void {
    this.task = newTask
    console.log('轮询任务已更新')
  }

  // 获取状态信息
  getStatus(): {
    isRunning: boolean
    isPaused: boolean
    intervalMs: number
    lastExecuted: Date | null
    nextExecution: Date | null
  } {
    return {
      isRunning: !!this.intervalId,
      isPaused: this.isPaused,
      intervalMs: this.intervalMs,
      lastExecuted: this.lastExecuted,
      nextExecution: this.nextExecution,
    }
  }
}
