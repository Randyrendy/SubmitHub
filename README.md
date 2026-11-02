# SubmitHub — Android Assignment Submission App

A native Android app (Kotlin, Room, Material Components) for lecturers to generate
time-limited submission codes and students to submit assignments against them.
Works fully offline — no backend server or internet needed.

## What it does

**Lecturer**
- Logs in, sees "My Courses" — a list of codes they've generated
- Generates a new code: fills in School → Faculty → Department → Course Code →
  Course Title → Assignment Title, picks an icon, picks a due date/time
- Gets a 6-character code (e.g. `X7K2Q9`) that auto-expires at the deadline
- Taps into any code to see who has submitted, with filename and timestamp

**Student**
- Logs in, types in a lecturer's code
- Sees the assignment's category card (course, department, due date, countdown)
- Picks a file from their device and submits — blocked automatically once the
  code has expired or if they've already submitted
- Can view "My Submissions" — a history of everything they've turned in

Demo accounts are seeded automatically the first time the app runs:
| Role     | ID          | Password   |
|----------|-------------|------------|
| Lecturer | `lecturer1` | `password` |
| Student  | `student1`  | `password` |

## How to run it (Android Studio)

1. Unzip this project.
2. Open **Android Studio** → **File → Open** → select the unzipped `SubmitHub` folder
   (the one containing `settings.gradle.kts`).
3. Let Gradle sync — the first sync needs internet to download the Android Gradle
   Plugin, Kotlin compiler, Room, Material, etc. This can take a few minutes.
4. Plug in your phone (with USB debugging on) or start an emulator.
5. Click the green **Run ▶** button.

That's it — no Firebase, no server, no API keys. Everything is stored locally on
the device with a Room (SQLite) database.

## Project structure

```
app/src/main/java/com/submithub/app/
├── MainActivity.kt                    # role picker: lecturer or student
├── data/
│   ├── entity/                        # UserEntity, CodeEntity, SubmissionEntity
│   ├── dao/                           # Room DAOs
│   ├── AppDatabase.kt                 # Room database + seed data
│   └── Repository.kt                  # single data access point
├── ui/lecturer/                       # login, dashboard, generate code, submissions
├── ui/student/                        # login, dashboard, submit, my submissions
└── util/                              # code generator, date helpers, icon catalog
```

## If something doesn't build

- **"Unresolved reference" on `ActivityXBinding` classes** — these are generated
  automatically by view binding on build. Do **Build → Clean Project** then
  **Build → Rebuild Project** and they'll regenerate.
- **Gradle sync fails** — check you have an internet connection; the first sync
  downloads dependencies from Google's and Maven's servers.
- **Old Android Studio version** — this project targets Android Gradle Plugin
  8.4.1 / Kotlin 1.9.24. If your Android Studio is older, let it prompt you to
  upgrade the Gradle plugin, or lower the versions in `build.gradle.kts` /
  `settings.gradle.kts` to match your installed AGP version.

## Notes for your presentation

- All data is local (Room/SQLite) — no network permission needed beyond reading
  files for the upload picker.
- Codes are functionally time-locked: once `expiresAt` has passed, the app
  blocks new submissions against that code, both visually (grayed-out button,
  "Expired" chip) and logically (checked again server-side... well, client-side,
  right before insert).
- A student can only submit once per code — enforced by a submission count check.
- The color palette (amber/gold) and every icon are original assets built as
  Android vector drawables — no external image files, so there's nothing to be
  missing when you unzip this.
