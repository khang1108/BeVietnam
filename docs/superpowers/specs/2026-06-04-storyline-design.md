# Design Specification: BeVietnam Storyline Roadmap Page

## Goal Description
Implement a responsive, visually stunning, and culturally-inspired "Storyline" (Roadmap) web page for the BeVietnam smart tourism platform.
The page will showcase a series of cultural and travel challenges (tasks) mapped out like a traditional Vietnamese accordion-folded book (Sách cổ). 

### Visual & Cultural Concepts (Hội An & Huế Vibe)
*   **Color Palette**: Warm ochre yellow (`#D49B2A`), glowing amber gold (`#E5B842`), brick terracotta/imperial red (`#B83C25`), charcoal/ink (`#221F1E`), and off-white parchment/paper (`#FDFBF7`).
*   **No SVG Icons**: Pure CSS styling, typography, borders, numbers, and letter badges will be used instead of standard icons for UI text or buttons.
*   **Aesthetic Detail**: A wood-framed accordion book mockup layout where pages expand/collapse smoothly. Faint overlay patterns (e.g. Dong Son bronze drum patterns) and polaroid-styled photo mounts for tasks.

---

## Proposed Changes

### 1. Mock Data Source
Create a mock data utility to represent tasks in the frontend.
*   **File**: `src/app/src/lib/mockTasks.ts` [NEW]
*   **Content**: A list of 5 challenge tasks mirroring the mobile app's Kotlin data:
    1.  **Thăm Chùa Một Cột** (Hà Nội, EASY)
    2.  **Thử món Bánh Mì Hội An** (Hội An, EASY)
    3.  **Học làm đèn lồng Hội An** (Hội An, MEDIUM)
    4.  **Khám phá hang động Tràng An** (Ninh Bình, MEDIUM)
    5.  **Leo núi Bà Nà Hills** (Đà Nẵng, HARD)

### 2. Localization Update
Update translation files to include bilingual support for the storyline page.
*   **File**: `src/app/src/i18n/translations.ts` [MODIFY]
*   **Content**: Add translation keys:
    *   `nav.storyline`: "Lộ trình" (vi) / "Storyline" (en)
    *   `storyline.title`, `storyline.subtitle`, `storyline.completeTask`, `storyline.completed`, `storyline.locked`, `storyline.reset`

### 3. Navigation Header Update
Add the Storyline link to the main navigation menu.
*   **File**: `src/app/src/components/layout/Navbar.tsx` [MODIFY]
*   **Content**: Add a new link to the `navItems` array linking to `/storyline`.

### 4. Storyline Route & Styling
*   **File**: `src/app/src/app/storyline/page.tsx` [NEW]
    *   Implements the page layout using React `useState` for active fold selection.
    *   Loads tasks from `mockTasks.ts`.
    *   Implements progress tracking stored in `localStorage` (`bevietnam-completed-tasks` and `bevietnam-active-task`).
    *   Implements interactive completion: completing task $N$ marks it done and automatically unlocks task $N+1$.
    *   Supports responsive structure: Desktop shows the horizontal accordion fold layout; Mobile stacks folds vertically for a scrollable layout.
*   **File**: `src/app/src/app/storyline/page.module.css` [NEW]
    *   Styles the wood-grain frame, paper texture shadow effects, polaroid frames, terracotta buttons, and progress indicators.
    *   Uses media queries to transition between horizontal layout (Desktop) and vertical stack (Mobile).

---

## Verification Plan

### Manual Verification
1.  **Run Development Server**: Start the Next.js app (`npm run dev`) and navigate to `http://localhost:3000/storyline`.
2.  **Visual Audit**: Verify the warm yellow/parchment colors, traditional serif headings (`Playfair Display`), wood frame outline, and polaroid-styled images. Ensure there are no SVG icons used for UI labels.
3.  **Functional Progression**:
    *   Verify that only Task 1 is unlocked initially (white/cream parchment), while Tasks 2-5 are greyed out with a "Khóa" stamp.
    *   Click "Hoàn Thành Nhiệm Vụ" on Task 1. Verify it marks Task 1 as "Đã Hoàn Thành" and automatically unlocks and opens Task 2.
    *   Refresh the page; verify the completion state and active page persist.
    *   Click "Đặt lại Hành trình" to reset progress back to the beginning.
4.  **Responsive Check**: Inspect and shrink the viewport to mobile width (width < 768px). Verify that the accordion folds stack vertically, headers write horizontally, and the details display properly in a mobile layout.
