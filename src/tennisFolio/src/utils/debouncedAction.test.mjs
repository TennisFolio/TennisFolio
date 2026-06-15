import assert from 'node:assert/strict';
import test from 'node:test';

import { createDebouncedAction } from './debouncedAction.js';

function createScheduler() {
  let nextId = 1;
  const timers = new Map();

  return {
    setTimeout(callback, delay) {
      const id = nextId;
      nextId += 1;
      timers.set(id, { callback, delay });
      return id;
    },
    clearTimeout(id) {
      timers.delete(id);
    },
    run(id) {
      timers.get(id)?.callback();
      timers.delete(id);
    },
    timers,
  };
}

test('debounces repeated calls and runs the last value after the delay', () => {
  const calls = [];
  const scheduler = createScheduler();
  const action = createDebouncedAction((value) => calls.push(value), 500, scheduler);

  action.schedule('first');
  const firstTimerId = Array.from(scheduler.timers.keys())[0];
  action.schedule('second');
  const secondTimerId = Array.from(scheduler.timers.keys())[0];

  assert.equal(scheduler.timers.size, 1);
  assert.equal(scheduler.timers.get(secondTimerId).delay, 500);
  assert.equal(scheduler.timers.has(firstTimerId), false);

  scheduler.run(secondTimerId);

  assert.deepEqual(calls, ['second']);
});

test('flush runs a pending debounced action immediately', () => {
  const calls = [];
  const scheduler = createScheduler();
  const action = createDebouncedAction((value) => calls.push(value), 500, scheduler);

  action.schedule({ gameId: 1 });
  action.flush();

  assert.deepEqual(calls, [{ gameId: 1 }]);
  assert.equal(scheduler.timers.size, 0);
});
