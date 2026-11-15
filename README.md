# SubmitHub

An Android app that makes assignment submission and grading simple for **lecturers** and **students**. Lecturers create courses and assignments, track deadlines, grade work with a rubric, and view analytics. Students browse their courses and submit assignments from their phones.

---

## Features

### For lecturers
- **Admin dashboard**: a central overview of courses, assignments, and submissions
- **Navigation drawer**: quick access to every lecturer screen
- **My Courses**: manage the courses you teach
- **All Assignments**: browse every assignment with **live search**
- **Grading screen**: grade submissions using a **four-criteria rubric**
- **Analytics screen**: **bar charts** showing performance and submission data
- **Calendar screen**: view assignments and **deadlines** by date

### For students
- Browse enrolled courses and assignments
- Submit files for an assignment
- Track deadlines

---

## Tech Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| IDE | Android Studio |
| Database | Firebase Firestore (cross-device sync) |
| File storage | Supabase Storage (free tier) |
| Networking | OkHttp |

### Architecture notes
- The app originally used a local **Room** database. It was migrated to **Firestore** so data syncs across devices.
- **Firebase Storage** was replaced by **Supabase Storage** for file uploads, to stay on a free tier.

---

## Getting Started

### Prerequisites
- Android Studio (latest stable)
- A Firebase project with Firestore enabled
- A Supabase project with a storage bucket
- An Android device or emulator

### Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd SubmitHub
   ```

2. **Open the project** in Android Studio and let Gradle sync finish.

3. **Connect Firebase**
   - In the Firebase Console, add an Android app using this project's package name.
   - Download `google-services.json`.
   - Place it in the `app/` folder.

4. **Connect Supabase**
   - Create a storage bucket for submissions.
   - Add your Supabase URL, API key, and bucket name where the app expects them (for example `local.properties` or a config file). Do not commit these keys.

5. **Run the app**
   - Select a device or emulator and click **Run**.

---

## Project Structure

```
app/
 └── src/main/java/.../
      ├── ui/          # Screens: dashboard, grading, analytics, calendar, courses, assignments
      ├── data/        # Firestore and Supabase access
      ├── model/       # Data classes
      └── util/        # Helpers
```

> Adjust the folder names above to match your actual package layout.

---

## Roadmap

- [ ] **Firestore seed guard**: prevent sample data from being seeded more than once
- [ ] **Parallel batch queries** for the Analytics screen to speed up loading
- [ ] **Streaming uploads via OkHttp** so large files are not buffered fully in RAM

---

## Testing

The app has been tested on a physical **Xiaomi** device. Please report bugs with clear steps to reproduce, the screen you were on, and what you expected to happen.

---

## Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push the branch: `git push origin feature/your-feature`
5. Open a pull request

---

## License

Add your license here (for example MIT).