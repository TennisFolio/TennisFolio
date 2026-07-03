# Codex Instructions

This repository is a Spring Boot application for TennisFolio.

Before changing code, read the documents that are relevant to the task:

- Root-level Korean markdown files: product/domain overview and shared vocabulary.
- `src/main/java/com/tennisfolio/Tennisfolio/matching/ENTITY_DESIGN.md`: competition, entry, game, and schedule design notes.

If a document appears garbled because of encoding, do not guess from broken text. Use the current source code and ask for clarification when the rule is important.

## Domain Language

Use these terms consistently in the matching module:

- `Competition`: the user-created competition/tournament unit. This is the main external resource and aggregate root for the local matching feature.
- `CompetitionEntry`: a participant entry in a competition.
- `Game`: a generated or recorded game inside a competition.
- `Schedule`: the generated arrangement of games. Use this for generated plans/results, not for the external creation resource.
- `Matching`: the internal algorithmic concern of making fair game combinations. Do not use it for controller names, application service names, or external API resource names.

## API Naming

- Create competitions with `POST /api/competitions`.
- Do not use `POST /api/matches` for competition creation.
- Reserve `/matches` or `/games` style resources for actual game/match lookup or management.
- Competition creation responses should return only the identifiers needed by the client:
  - `publicId`
  - `editToken`

## Java Naming

- Controller: `CompetitionController`
- Application command service: `CompetitionCommandService`
- Request DTO: `CompetitionCreateRequest`
- Response DTO: `CompetitionCreateResponse`
- Creation method: `createCompetition`
- Schedule generation method: `generateSchedule`

Keep `Matching` names inside algorithm/domain internals only when they describe candidate generation or match type logic, for example `MatchCandidate` or `MatchType`.

## Entity Mapping Notes

- `tb_competition.PUBLIC_ID` maps to `Competition.publicId`.
- `tb_competition.EDIT_TOKEN` maps to `Competition.editToken`.
- `publicId` is the URL-safe public identifier.
- `editToken` is the client-held edit authorization token.

## Working Rules

- Prefer existing package structure and coding style.
- Keep refactors scoped to the requested domain boundary.
- Do not rename generated or unrelated legacy modules unless the task requires it.
- Do not create git commits by default. Only commit changes when the user explicitly asks for a commit.
- Even when the user has asked for a commit, show the staged file list, test/verification result, and commit message first, then ask for confirmation before running `git commit`.
- When the user asks to develop a feature or proceed with a phase, read `docs/workflows/development-workflow.md` first and follow that workflow before making implementation changes.
- Write documents under `docs/superpowers/specs` and `docs/superpowers/plans` in Korean unless the user explicitly asks for another language.
- When editing Competition UI styles, follow `docs/competition-design-system.md` and use CSS variables from `src/tennisFolio/src/styles/competition-theme.css`. Do not introduce new hard-coded Competition colors unless the design system document and theme variables are updated in the same change.
- When doing frontend development, do not create or update frontend test code by default. Only write frontend tests when the user explicitly asks for test code.
- When doing screen/frontend development, do not run frontend tests after implementation by default. Only run frontend tests when the user explicitly asks for testing or grants permission for that specific test command.
- Do not run Java/Gradle builds or npm builds by default. This includes `.\gradlew.bat compileJava`, `.\gradlew.bat compileTestJava`, `.\gradlew.bat build`, `npm run build`, and similar build commands.
- Only run Java/Gradle or npm build commands when the user explicitly asks for verification, asks to run a build/test, or grants permission for that specific command.
- When build verification is skipped because of this rule, state that it was skipped in the final response.
