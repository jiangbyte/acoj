// Implementation note.
export class PollingTool {
  private intervalId: NodeJS.Timeout | null = null
  private isPaused: boolean = false
  private lastExecuted: Date | null = null
  private nextExecution: Date | null = null

  constructor(
    private task: () => void | Promise<void>,
    private intervalMs: number = 5000,
  ) {}

  // Implementation note.
  start(): void {
    if (this.intervalId) {
      console.warn('Polling is already running')
      return
    }

    this.isPaused = false
    this.executeTask() // Implementation note.

    this.intervalId = setInterval(() => {
      if (!this.isPaused) {
        this.executeTask()
      }
    }, this.intervalMs)

    this.calculateNextExecution()
    console.log('Polling started')
  }

  // Implementation note.
  private async executeTask(): Promise<void> {
    try {
      this.lastExecuted = new Date()
      await this.task()
    }
    catch (error) {
      console.error('Polling task failed:', error)
    }
    finally {
      this.calculateNextExecution()
    }
  }

  // Implementation note.
  private calculateNextExecution(): void {
    if (this.lastExecuted) {
      this.nextExecution = new Date(this.lastExecuted.getTime() + this.intervalMs)
    }
  }

  // Implementation note.
  pause(): void {
    if (!this.intervalId) {
      console.warn('Polling is not running and cannot be paused')
      return
    }

    this.isPaused = true
    console.log('Polling paused')
  }

  // Implementation note.
  resume(): void {
    if (!this.intervalId) {
      console.warn('Polling is not running and cannot be resumed')
      return
    }

    if (!this.isPaused) {
      console.warn('Polling is not paused')
      return
    }

    this.isPaused = false
    // Implementation note.
    this.executeTask()
    console.log('Polling resumed')
  }

  // Implementation note.
  stop(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId)
      this.intervalId = null
    }

    this.isPaused = false
    this.lastExecuted = null
    this.nextExecution = null
    console.log('Polling stopped')
  }

  // Implementation note.
  updateInterval(newIntervalMs: number): void {
    if (newIntervalMs <= 0) {
      throw new Error('Polling interval must be greater than 0')
    }

    const wasRunning = !!this.intervalId && !this.isPaused

    if (this.intervalId) {
      this.stop()
    }

    this.intervalMs = newIntervalMs

    if (wasRunning) {
      this.start()
    }

    console.log(`Polling interval updated to: ${newIntervalMs}ms`)
  }

  // Implementation note.
  updateTask(newTask: () => void | Promise<void>): void {
    this.task = newTask
    console.log('Polling task updated')
  }

  // Implementation note.
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
