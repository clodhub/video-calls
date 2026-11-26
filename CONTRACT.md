# Video calls

## Purpose and facilities
Lets the people using an application see and hear each other while they use it, the way friends around a real card table would. Each shared activity (a game table, a room) gets its own video room on a LiveKit video service. The application's server hands each person a pass for their table's room; their browser, iPhone, iPad, Mac, Apple TV or Android app joins with it, shows the others' video beside the activity and plays their sound.

It looks after the parts that are easy to get wrong:

- A person who is only watching can see and hear but not send video or sound.
- A shared screen (a television showing the table, see display-mode) never gets a pass and turns the camera and microphone off at once.
- A refused camera leaves the person in the room with sound only.
- Muting the microphone, turning the camera off and turning the speaker off are remembered when the person comes back.
- The application can pause the call after five idle minutes and resume it with the same pass.

Passes last six hours and are signed on the server with the video service's secret, which never reaches a device. Without a video service configured, nobody gets a pass and the rest of the application works as before.

Facilities: `media-session` (Video and sound between the people in a room, on the web, Apple devices and Android, through a LiveKit video service).

## User inputs
The installer collects four settings:

- `enabled`: whether the application offers video, true or false (default true); `false` means no passes are issued.
- `serviceUrl`: the address of the LiveKit video service, an http or https address (optional, no default), for example `https://my-project.livekit.cloud`. Devices connect to it as `wss://` (or `ws://` for http). Leave it empty to add it later; until then there is no video.
- `keyReference` and `secretReference`: secret references (optional), the names under which the LiveKit API key and API secret are kept in the deployment's secret store, written as letters, digits and underscores starting with a letter. These are names only, never the key or secret themselves.

The server needs all three of the address, the key and the secret; if any is missing there is no video. The deployment turns the settings and the two secrets into `LIVEKIT_URL`, `LIVEKIT_API_KEY` and `LIVEKIT_API_SECRET` (the environment the server reads) with `videoEnvironment(settings, secrets)`, or sets those three variables directly.

The application also supplies, in code, the room name prefix (the same one its rooms use, including the separator, for example `ourpoker-`), who each person is and whether they may send video, and on the web a function saying whether this device may capture (false on a shared screen), whether sound is on, and display-mode's event name.

## Artefacts and dependencies
Every implementation file maps to `components/video-calls/` with its `implementation/` prefix removed: `server/video.ts` (passes and room listing, using only the platform's built-in cryptography), `web/video.ts` (the browser `Video` class over livekit-client 2.23.0), Swift and Kotlin `VideoSession`, and the verification helpers. SOURCE.md records where each file came from and SOURCE.json pins the source hashes; `package-lock.json` locks the browser library and TypeScript, and `tests/native/dependencies.json` identifies the locked Apple and Android LiveKit libraries and their checksums.

## Provided interfaces
`interfaces/media-session.json` is an OpenRPC 1.3.2 document for `com.clodhub/media-session` version 1.0.0: one method per function an application calls, tagged `server` (`server/video.ts`), `web` (`web/video.ts`, the `Video` class and its methods), `swift` and `kotlin` (`VideoSession.*`), with types as JSON Schema 2020-12. `interfaces/media-session-verification.json` is a second OpenRPC 1.3.2 document for the verification helpers that clod's checks call.

## Required interfaces
`video-calls-configuration` (`com.clodhub/video-calls-configuration` version 1.0.0) is optional: the values above can come from the application's own code. `interfaces/required-video-calls-configuration.json` is the JSON Schema 2020-12 document of what the application supplies: the settings, the environment, the room prefix, the participant and the browser `Video` options.

## Integration
On the server, when a person asks to join their table's video:

```ts
import { videoGrant } from "./components/video-calls/server/index";
const grant = await videoGrant(env, tableId, player.id, player.name, Date.now(), !player.spectator, "ourpoker-");
// null when video is not configured; otherwise { url, token, room }
```

Never issue a pass to a shared screen. In the browser, create one `Video` with a function to call when its state changes and `{ canCapture, soundOn, modeEvent }`, then `connect(grant.url, grant.token, grant.room)`. Use `setMic`, `setCam` and `setSpeaker` for the person's buttons, `hold(true)` while they step away, `armIdle()` to pause after five idle minutes and `resume()` to come back, and `close()` when leaving. `monitor` and `cell` give the video tile to place on screen. Apple and Android use `VideoSession` the same way; when a device becomes a shared screen, call its disconnect. `listRooms` lists the application's rooms on the video service and who is in each. `examples/consumer.ts` is a complete example.

## Visual definition and connected examples
`visual/definition.json` is the component block. `visual/examples/connected.json` wires an illustrative configuration provider and consumer. `visual/output/connected.svg` and `connected.png` are the rendered diagram and `visual/output/description.md` its text equivalent.

## Infrastructure
A LiveKit video service and its API key and secret, supplied by the application's owner. LiveKit Cloud's free Build plan allows 100 people connected at once and 5,000 participant minutes a month, with no card; the paid Ship plan (from $50 a month) allows 1,000. The open-source LiveKit server can also be run on your own hardware. Each device asks the person for camera and microphone permission.

## Verification
The required checks run without a video service or camera:

- `node --test tests/video.test.mjs`: passes carry the right person, room and six-hour lifetime with a valid signature, a watcher cannot send, missing or disabled settings give no pass, room listing keeps to the application's prefix, and the browser `Video` keeps preferences through hold, sound changes, idle pause and cleanup, never captures after a switch to a shared screen, and stays in the room when the camera is refused.
- `npm run typecheck`: the browser and server code type-check.
- `node --test tests/provider.test.mjs`: the live-service check behaves correctly against a stand-in service, including cleaning up only the rooms it created and exposing no secret in its errors.
- `node --test tests/browser-process.test.mjs`: the headless browser the media checks start is confined to its own profile and is always stopped.
- The Python checks of the Apple result reader, the native check runner and the locked Android dependency list.

The platform checks build the Apple app for the iPhone, Apple TV and Mac simulators and the Android test app, and run their media tests (they need Xcode or the Android tools). The deployed check, `provider-media`, joins a real LiveKit service with test participants; it needs the owner's service and keys.

## Scenarios and presentation
Five friends play from five homes. Each sees the others' faces in small tiles round the table and hears them talk. One joins only to watch: they see and hear everyone, but their own camera never turns on. The table is also on the living-room television: it shows the cards, not the video, and its camera stays off. A player whose laptop refuses the camera still talks and listens. A player who mutes, leaves for a drink and returns is still muted.

## Updates and removal
Updates replace the component's files. Each person's microphone, camera and speaker choices live in the application's storage and are not deleted on removal; neither are the rooms on the video service, which LiveKit closes when they are empty.
