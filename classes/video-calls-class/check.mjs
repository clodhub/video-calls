import { createHmac } from 'node:crypto';
import { assert, load } from './runtime.mjs';
const m = await load('server/index.ts');
const env = { LIVEKIT_URL: 'wss://media.example', LIVEKIT_API_KEY: 'class-test', LIVEKIT_API_SECRET: 'class-local-fixture' };
assert.equal(await m.videoGrant({}, 'room', 'person', 'Ada', 1000, true, 'class-'), null);
for (const publish of [true, false]) {
  const grant = await m.videoGrant(env, 'room', 'person', 'Ada', 1000, publish, 'class-');
  const [head, body, signature] = grant.token.split('.');
  const payload = JSON.parse(Buffer.from(body, 'base64url'));
  assert.equal(signature, createHmac('sha256', env.LIVEKIT_API_SECRET).update(`${head}.${body}`).digest('base64url'));
  assert.equal(payload.sub, 'person');
  assert.equal(payload.video.room, 'class-room');
  assert.equal(payload.video.canPublish, publish);
  assert.equal(payload.video.canPublishData, false);
  assert.equal(payload.exp - payload.iat, 21600);
}
