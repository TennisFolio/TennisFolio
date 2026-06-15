const defaultScheduler = {
  setTimeout: globalThis.setTimeout.bind(globalThis),
  clearTimeout: globalThis.clearTimeout.bind(globalThis),
};

export function createDebouncedAction(action, delay, scheduler = defaultScheduler) {
  let timerId = null;
  let latestValue;

  const cancel = () => {
    if (timerId === null) {
      return;
    }

    scheduler.clearTimeout(timerId);
    timerId = null;
  };

  const run = () => {
    const value = latestValue;
    latestValue = undefined;
    timerId = null;
    action(value);
  };

  return {
    schedule(value) {
      latestValue = value;
      cancel();
      timerId = scheduler.setTimeout(run, delay);
    },
    flush() {
      if (timerId === null) {
        return;
      }

      cancel();
      run();
    },
    cancel,
  };
}
