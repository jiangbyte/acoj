interface PollingOptions {
  // Implementation note.
  interval: number
  // Implementation note.
  immediate?: boolean
}

interface PollingCallbacks<T = any> {
  // Implementation note.
  onPoll: () => T | Promise<T>
  // Implementation note.
  onStart?: () => void
  // Implementation note.
  onPause?: () => void
  // Implementation note.
  onStop?: () => void
}

export class Poller<T = any> {
  private interval: number
  private timer: NodeJS.Timeout | null = null
  private isActive = false
  private isPaused = false

  private onPoll: () => T | Promise<T>
  private onStart?: () => void
  private onPause?: () => void
  private onStop?: () => void

  constructor(options: PollingOptions, callbacks: PollingCallbacks<T>) {
    this.interval = options.interval
    this.onPoll = callbacks.onPoll
    this.onStart = callbacks.onStart
    this.onPause = callbacks.onPause
    this.onStop = callbacks.onStop

    if (options.immediate) {
      this.start()
    }
  }

  /**
    * Implementation details.
    * Implementation details.
   */
  start(onStart?: () => void): void {
    if (this.isActive) {
      console.warn('Poller is already running')
      return
    }

    this.isActive = true
    this.isPaused = false

    // Implementation note.
    const startCallback = onStart || this.onStart
    if (startCallback) {
      try {
        startCallback()
      }
      catch (error) {
        console.error('Failed to run polling start callback:', error)
      }
    }

    this.executePoll()
  }

  /**
    * Implementation details.
    * Implementation details.
   */
  pause(onPause?: () => void): void {
    if (!this.isActive || this.isPaused) {
      return
    }

    this.isPaused = true
    this.clearTimer()

    // Implementation note.
    const pauseCallback = onPause || this.onPause
    if (pauseCallback) {
      try {
        pauseCallback()
      }
      catch (error) {
        console.error('Failed to run polling pause callback:', error)
      }
    }
  }

  /**
    * Implementation details.
   */
  resume(): void {
    if (!this.isActive || !this.isPaused) {
      return
    }

    this.isPaused = false
    this.executePoll()
  }

  /**
    * Implementation details.
    * Implementation details.
   */
  stop(onStop?: () => void): void {
    if (!this.isActive) {
      return
    }

    this.isActive = false
    this.isPaused = false
    this.clearTimer()

    // Implementation note.
    const stopCallback = onStop || this.onStop
    if (stopCallback) {
      try {
        stopCallback()
      }
      catch (error) {
        console.error('Failed to run polling stop callback:', error)
      }
    }
  }

  /**
    * Implementation details.
    * Implementation details.
   */
  updateInterval(newInterval: number): void {
    this.interval = newInterval

    // Implementation note.
    if (this.isActive && !this.isPaused) {
      this.clearTimer()
      this.executePoll()
    }
  }

  /**
    * Implementation details.
    * Implementation details.
   */
  updatePollCallback(onPoll: () => T | Promise<T>): void {
    this.onPoll = onPoll
  }

  /**
    * Implementation details.
   */
  getStatus(): { isActive: boolean, isPaused: boolean, interval: number } {
    return {
      isActive: this.isActive,
      isPaused: this.isPaused,
      interval: this.interval,
    }
  }

  /**
    * Implementation details.
   */
  async executeImmediately(): Promise<T | null> {
    if (!this.isActive || this.isPaused) {
      return null
    }

    return this.executePollTask()
  }

  private executePoll(): void {
    this.clearTimer()

    this.timer = setTimeout(async () => {
      if (this.isActive && !this.isPaused) {
        await this.executePollTask()
        this.executePoll()
      }
    }, this.interval)
  }

  private async executePollTask(): Promise<T> {
    try {
      const result = await this.onPoll()
      return result
    }
    catch (error) {
      console.error('Polling task failed:', error)
      throw error
    }
  }

  private clearTimer(): void {
    if (this.timer) {
      clearTimeout(this.timer)
      this.timer = null
    }
  }

  /**
    * Implementation details.
   */
  destroy(): void {
    this.stop()
  }
}

// Implementation note.
// async function example() {
//   let count = 0;

// Implementation note.
//   const poller = new Poller(
//     {
//       interval: 2000,
//       immediate: false
//     },
//     {
//       onPoll: async () => {
//         count++;
// Implementation note.
// Implementation note.
//       },
//       onStart: () => {
// Implementation note.
//       },
//       onPause: () => {
// Implementation note.
//       },
//       onStop: () => {
// Implementation note.
//       }
//     }
//   );

// Implementation note.
//   poller.start();

// Implementation note.
//   setTimeout(() => {
//     poller.pause(() => {
// Implementation note.
//     });
//   }, 10000);

// Implementation note.
//   setTimeout(() => {
//     poller.resume();
//   }, 15000);

// Implementation note.
//   setTimeout(() => {
//     poller.updateInterval(1000);
// Implementation note.
//   }, 20000);

// Implementation note.
//   setTimeout(() => {
//     poller.stop(() => {
// Implementation note.
//     });
//   }, 25000);

// Implementation note.
//   setTimeout(async () => {
//     const result = await poller.executeImmediately();
// Implementation note.
//   }, 5000);
// }

// Implementation note.
// // example();
