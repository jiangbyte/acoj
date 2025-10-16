interface PollingOptions {
  // 轮询间隔（毫秒）
  interval: number
  // 是否立即开始执行第一次轮询
  immediate?: boolean
}

interface PollingCallbacks<T = any> {
  // 轮询期间要执行的函数
  onPoll: () => T | Promise<T>
  // 开始轮询时要执行的函数
  onStart?: () => void
  // 暂停轮询时要执行的函数
  onPause?: () => void
  // 停止轮询时要执行的函数
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
   * 开始轮询
   * @param onStart 可选，开始轮询时要执行的函数
   */
  start(onStart?: () => void): void {
    if (this.isActive) {
      console.warn('轮询器已经在运行中')
      return
    }

    this.isActive = true
    this.isPaused = false

    // 执行开始回调
    const startCallback = onStart || this.onStart
    if (startCallback) {
      try {
        startCallback()
      }
      catch (error) {
        console.error('开始轮询回调执行失败:', error)
      }
    }

    this.executePoll()
  }

  /**
   * 暂停轮询
   * @param onPause 可选，暂停轮询时要执行的函数
   */
  pause(onPause?: () => void): void {
    if (!this.isActive || this.isPaused) {
      return
    }

    this.isPaused = true
    this.clearTimer()

    // 执行暂停回调
    const pauseCallback = onPause || this.onPause
    if (pauseCallback) {
      try {
        pauseCallback()
      }
      catch (error) {
        console.error('暂停轮询回调执行失败:', error)
      }
    }
  }

  /**
   * 恢复轮询
   */
  resume(): void {
    if (!this.isActive || !this.isPaused) {
      return
    }

    this.isPaused = false
    this.executePoll()
  }

  /**
   * 停止轮询
   * @param onStop 可选，停止轮询时要执行的函数
   */
  stop(onStop?: () => void): void {
    if (!this.isActive) {
      return
    }

    this.isActive = false
    this.isPaused = false
    this.clearTimer()

    // 执行停止回调
    const stopCallback = onStop || this.onStop
    if (stopCallback) {
      try {
        stopCallback()
      }
      catch (error) {
        console.error('停止轮询回调执行失败:', error)
      }
    }
  }

  /**
   * 更新轮询间隔
   * @param newInterval 新的轮询间隔（毫秒）
   */
  updateInterval(newInterval: number): void {
    this.interval = newInterval

    // 如果正在运行且没有暂停，重新启动计时器
    if (this.isActive && !this.isPaused) {
      this.clearTimer()
      this.executePoll()
    }
  }

  /**
   * 更新轮询函数
   * @param onPoll 新的轮询函数
   */
  updatePollCallback(onPoll: () => T | Promise<T>): void {
    this.onPoll = onPoll
  }

  /**
   * 获取轮询器状态
   */
  getStatus(): { isActive: boolean, isPaused: boolean, interval: number } {
    return {
      isActive: this.isActive,
      isPaused: this.isPaused,
      interval: this.interval,
    }
  }

  /**
   * 立即执行一次轮询任务
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
      console.error('轮询任务执行失败:', error)
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
   * 销毁轮询器，释放资源
   */
  destroy(): void {
    this.stop()
  }
}

// // 使用示例
// async function example() {
//   let count = 0;

//   // 创建轮询器
//   const poller = new Poller(
//     {
//       interval: 2000,
//       immediate: false
//     },
//     {
//       onPoll: async () => {
//         count++;
//         console.log(`执行第 ${count} 次轮询`);
//         return { data: `第 ${count} 次结果` };
//       },
//       onStart: () => {
//         console.log('轮询开始');
//       },
//       onPause: () => {
//         console.log('轮询暂停');
//       },
//       onStop: () => {
//         console.log('轮询停止');
//       }
//     }
//   );

//   // 开始轮询
//   poller.start();

//   // 10秒后暂停
//   setTimeout(() => {
//     poller.pause(() => {
//       console.log('自定义暂停回调');
//     });
//   }, 10000);

//   // 15秒后恢复
//   setTimeout(() => {
//     poller.resume();
//   }, 15000);

//   // 20秒后更新间隔
//   setTimeout(() => {
//     poller.updateInterval(1000);
//     console.log('轮询间隔更新为 1 秒');
//   }, 20000);

//   // 25秒后停止
//   setTimeout(() => {
//     poller.stop(() => {
//       console.log('自定义停止回调');
//     });
//   }, 25000);

//   // 立即执行一次轮询任务
//   setTimeout(async () => {
//     const result = await poller.executeImmediately();
//     console.log('立即执行结果:', result);
//   }, 5000);
// }

// // 运行示例
// // example();
