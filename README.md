# Programming Individual Project – Mini Blackboard System

## 📘 Overview
This project is a **console-based mini Blackboard system** built in **Java**.  
It simulates a simplified version of an academic course management platform, allowing **administrators**, **teachers**, and **students** to interact within a structured object-oriented environment.

The project follows strong **OOP principles** including:
- Abstraction  
- Inheritance  
- Interfaces  
- Composition  
- Encapsulation  

It was developed as part of the **Programming II** course assignment to demonstrate software design, UML translation, and practical class hierarchy implementation.



Here was my planning process history
________
Fri Oct 31st Plan:
Chatgpt suggested i should use github (hence why we're here) and i should make a feature and hotfix branch.

It is saying i should do "one feature branch per self-contained feature (a small, reviewable slice)"

It also suggested i should do this roadmap for the features which i find really useful and probably will do:

## 🗺️ Implementation Lifecycle (Roadmap)
1. **`feature/base-models`**  
   Add fields + basic constructors/getters + simple `toString()` across:
   `User/Admin/Teacher/Student/Course/Assignment/Submission/Enrollment/Announcement`.

2. **`feature/enrollment-rules`**  
   `Course.hasCapacity()`, prerequisites, `Admin.enrollStudent(course, student)`, `Enrollment` creation.  
   (Optionally `Course.addStudent(Student)` as a helper.)

3. **`feature/submission-flow`**  
   `Assignment.submit(student, content)` → creates `Submission`; `Submission.isLate()`; set `submittedAt`.

4. **`feature/grading`**  
   `Teacher.recordGrade(assignment, student, score)`; `Enrollment.computeFinal()` (weights × scores).

5. **`feature/announcements`**  
   `Teacher.postAnnouncement(course, text)`; `Course.getAnnouncements()`; student can list per course.

6. **`feature/console-ui`**  
   Minimal menus to demo: create course, enroll, create/submit assignment, grade, list announcements.

7. **`feature/persistence-stub` (optional)**  
   Wire `Persistable.save()/load()` with simple prints or CSV/JSON stubs (`DataManager` if desired).

8. **`feature/validation-and-errors`**  
   Guard rails + friendly messages: capacity full, already enrolled, missing prerequisites, etc.

*If a feature grows, split it (e.g., `feature/assign-create`, `feature/assign-submit`, `feature/submission-late-logic`).*


___



  

