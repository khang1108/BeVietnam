# 📐 BeVietnam — Team Code Standards

> **Version:** 1.0 | **Cập nhật lần cuối:** 2026-05-20  
> Tài liệu này là **bắt buộc** cho mọi thành viên trong nhóm. Mọi PR không tuân thủ sẽ bị từ chối.

---

## Mục lục

1. [Nguyên tắc chung](#1-nguyên-tắc-chung)
2. [Git Workflow](#2-git-workflow)
3. [Quy tắc đặt tên](#3-quy-tắc-đặt-tên)
4. [Backend (Python / FastAPI)](#4-backend-python--fastapi)
5. [Web (TypeScript / Next.js)](#5-web-typescript--nextjs)
6. [Mobile (Kotlin / Jetpack Compose)](#6-mobile-kotlin--jetpack-compose)
7. [Viết Comment & Docstring](#7-viết-comment--docstring)
8. [Testing](#8-testing)
9. [Code Review Checklist](#9-code-review-checklist)
10. [Những điều tuyệt đối KHÔNG làm](#10-những-điều-tuyệt-đối-không-làm)

---

## 1. Nguyên tắc chung

### 1.1 Các nguyên tắc cốt lõi

| Nguyên tắc | Mô tả |
|-----------|-------|
| **Clarity over Cleverness** | Code dễ đọc quan trọng hơn code "thông minh". Người mới vào nhóm phải đọc được trong 5 phút. |
| **Single Responsibility** | Mỗi file, class, function chỉ làm **một việc**. |
| **Fail Loud** | Lỗi phải được raise/throw rõ ràng — không bao giờ im lặng nuốt exception. |
| **No Magic Numbers** | Mọi hằng số phải có tên (`MAX_RETRIES = 3` thay vì `3`). |
| **DRY nhưng không quá sớm** | Đừng abstract khi chỉ có 1-2 use case. Đợi lần thứ 3 mới refactor. |

### 1.2 Quy tắc về file

- Mỗi file **tối đa 300 dòng**. Vượt quá → tách file.
- Mỗi function/method **tối đa 40 dòng**. Vượt quá → tách function.
- Không để file rỗng (chỉ có `pass` hoặc `// TODO`). Phải có ít nhất stub cơ bản.

---

## 2. Git Workflow

### 2.1 Branch Naming

```
<type>/<ticket-or-description>

Ví dụ:
feat/auth-jwt-login
fix/explore-filter-crash
refactor/backend-schema-split
docs/update-contributing
chore/upgrade-dependencies
```

| Prefix | Khi nào dùng |
|--------|--------------|
| `feat/` | Tính năng mới |
| `fix/` | Sửa bug |
| `refactor/` | Tái cấu trúc code (không thêm tính năng) |
| `docs/` | Chỉ thay đổi tài liệu |
| `chore/` | Config, dependencies, build tools |
| `test/` | Thêm/sửa tests |

### 2.2 Commit Message

Tuân theo **Conventional Commits**:

```
<type>(<scope>): <mô tả ngắn gọn bằng tiếng Anh>

[body tùy chọn — giải thích WHY, không phải WHAT]

[footer: BREAKING CHANGE, Closes #issue]
```

```bash
# ✅ Đúng
feat(auth): add JWT refresh token endpoint
fix(explore): correct place category filter logic
refactor(backend): split monolithic schemas.py into domain files
docs(readme): add local dev setup instructions

# ❌ Sai
update code
fix bug
thêm tính năng mới
WIP
```

**Quy tắc commit:**
- Dòng đầu **tối đa 72 ký tự**
- Dùng **tiếng Anh**, viết thường, **không dấu chấm** cuối
- Body viết bằng tiếng Việt hoặc Anh đều được

### 2.3 Pull Request Rules

- **Không merge PR của chính mình** — phải có ít nhất 1 người review
- PR title theo cùng format Conventional Commits
- Mọi PR phải có **description** mô tả: làm gì, tại sao, test như thế nào
- PR **tối đa 400 dòng thay đổi**. Lớn hơn → tách thành nhiều PR nhỏ
- Resolve tất cả conflict trước khi request review

### 2.4 Branch Protection

- `main`: Protected. Chỉ merge qua PR, bắt buộc CI pass.
- `develop`: Integration branch. Merge từ feature branches.
- Feature branches: Merge vào `develop`, không merge thẳng vào `main`.

---

## 3. Quy tắc đặt tên

### 3.1 Quy ước chung theo ngôn ngữ

| Ngôn ngữ | Variables & Functions | Classes | Constants | Files |
|----------|----------------------|---------|-----------|-------|
| **Python** | `snake_case` | `PascalCase` | `UPPER_SNAKE_CASE` | `snake_case.py` |
| **TypeScript** | `camelCase` | `PascalCase` | `UPPER_SNAKE_CASE` | `kebab-case.ts` |
| **Kotlin** | `camelCase` | `PascalCase` | `UPPER_SNAKE_CASE` | `PascalCase.kt` |

### 3.2 Đặt tên có ý nghĩa

```python
# ❌ Sai — không rõ nghĩa
def get(x, y):
    d = x - y
    return d

# ✅ Đúng — tự giải thích
def calculate_distance_km(lat1: float, lat2: float) -> float:
    delta_lat = lat1 - lat2
    return delta_lat
```

```typescript
// ❌ Sai
const d = new Date();
const u = users.filter(x => x.a);

// ✅ Đúng
const currentDate = new Date();
const activeUsers = users.filter(user => user.isActive);
```

### 3.3 Boolean naming

Boolean phải bắt đầu bằng `is`, `has`, `can`, `should`:

```python
# ✅
is_active: bool
has_permission: bool
can_edit: bool
should_retry: bool

# ❌
active: bool
permission: bool
```

### 3.4 Naming theo layer (Backend)

| Layer | Hậu tố | Ví dụ |
|-------|--------|-------|
| Schema | `Request`, `Response`, `Schema` | `PlaceSchema`, `LoginRequest` |
| Model (DB) | `Model` | `PlaceModel`, `UserModel` |
| Repository | `Repository` | `PlaceRepository` |
| Service | `Service` | `CaptureService` |
| Router | `router` (biến) | `router = APIRouter()` |

---

## 4. Backend (Python / FastAPI)

### 4.1 Cấu trúc thư mục

```
backend/app/
├── api/endpoints/      ← HTTP layer ONLY (routing + serialization)
├── core/               ← Config, external clients (AI Core)
├── models/             ← SQLAlchemy DB models
├── repositories/       ← DB access (CRUD operations)
├── services/           ← Business logic
└── schemas/            ← Pydantic schemas, 1 file per domain
```

**Quy tắc tuyệt đối:**
- `endpoints/` chỉ được gọi `services/` — **không được** gọi `repositories/` trực tiếp
- `services/` chỉ được gọi `repositories/` — **không được** import từ `endpoints/`
- `schemas/` không import gì từ các layer khác

### 4.2 Endpoint style

```python
# ✅ Đúng — endpoint chỉ làm routing + validation
@router.post("/captures", response_model=CaptureResponse, status_code=201, tags=["Captures"])
async def create_capture(body: CaptureCreateRequest):
    """POST /captures — Lưu metadata ảnh chụp."""
    return await capture_service.create_capture(body)

# ❌ Sai — logic nghiệp vụ trong endpoint
@router.post("/captures")
async def create_capture(body: CaptureCreateRequest):
    now = datetime.now(timezone.utc)
    capture = CaptureResponse(id=str(uuid.uuid4()), ...)
    _captures.append(capture)
    return capture
```

### 4.3 Type hints bắt buộc

```python
# ✅ Bắt buộc
async def get_places(
    category: Optional[str] = None,
    limit: int = 10,
) -> tuple[int, list[PlaceSchema]]:
    ...

# ❌ Không chấp nhận
async def get_places(category=None, limit=10):
    ...
```

### 4.4 Error handling

```python
# ✅ Dùng HTTPException với status code rõ ràng
from fastapi import HTTPException

if not place:
    raise HTTPException(status_code=404, detail=f"Place '{place_id}' not found")

# ❌ Trả về None hoặc empty dict khi lỗi
if not place:
    return {}
```

### 4.5 Environment variables

- **Không bao giờ** hardcode credentials, API keys, URLs trong code
- Mọi config phải qua `Settings` class trong `core/config.py`
- File `.env` **không được** commit vào Git

```python
# ✅
AI_CORE_BASE_URL: str = "http://ai-core:8001"  # default, overridden by .env

# ❌
url = "http://localhost:8001"  # hardcode
```

---

## 5. Web (TypeScript / Next.js)

### 5.1 Cấu trúc thư mục

```
web/src/
├── app/                ← Route wrappers ONLY (thin, max 5 lines)
├── features/           ← Feature modules (UI + logic)
│   └── <feature>/
│       ├── components/ ← React components
│       ├── hooks/      ← Feature-specific hooks
│       ├── styles/     ← CSS Modules cho feature này
│       └── index.ts    ← Public API (barrel file)
├── components/         ← Shared UI components
├── hooks/              ← Shared hooks
├── lib/                ← API client, utilities
├── i18n/               ← Translations
└── styles/             ← Global CSS
```

### 5.2 Component rules

```tsx
// ✅ Named export (không dùng default export cho components)
export function LoginForm() { ... }

// ❌ Default export
export default function LoginForm() { ... }

// ✅ Props interface rõ ràng
interface PlaceCardProps {
    place: Place;
    isHighlighted?: boolean;
    onSelect: (place: Place) => void;
}

export function PlaceCard({ place, isHighlighted = false, onSelect }: PlaceCardProps) {
    ...
}
```

### 5.3 App router — Thin Wrapper pattern

```tsx
// ✅ app/explore/page.tsx — CHỈ 5 dòng, không có UI logic
import { ExplorePage } from '@/features/explore';

export default function Page() {
    return <ExplorePage />;
}

// ❌ Viết UI trực tiếp trong app/ route file
export default function Page() {
    return (
        <div className={styles.container}>
            ... 100 dòng JSX ...
        </div>
    );
}
```

### 5.4 Barrel file — index.ts

Mỗi feature **bắt buộc** có `index.ts` export public API:

```typescript
// features/auth/index.ts
export { LoginForm } from './components/LoginForm';
export { RegisterForm } from './components/RegisterForm';
// Không export internal components hay hooks
```

### 5.5 Import paths

Luôn dùng path alias `@/`, không dùng relative path:

```typescript
// ✅
import { useI18n } from '@/i18n';
import styles from '@/styles/pages.module.css';

// ❌
import { useI18n } from '../../../i18n';
import styles from '../../../../styles/pages.module.css';
```

### 5.6 CSS Modules

- **Không** dùng inline styles (trừ dynamic values như `transform: rotate(${deg}deg)`)
- **Không** dùng Tailwind (dự án dùng CSS Modules)
- Class names: `camelCase` trong CSS Modules

```tsx
// ✅
<div className={styles.authCard}>

// ❌
<div style={{ background: '#fff', padding: '16px' }}>
```

---

## 6. Mobile (Kotlin / Jetpack Compose)

### 6.1 Cấu trúc package

```
com.bevietnam/
├── core/
│   ├── model/          ← Domain entities (thuần Kotlin, không Android dep)
│   ├── domain/
│   │   ├── repository/ ← Interfaces (I prefix)
│   │   └── usecase/    ← Business logic
│   ├── data/
│   │   ├── remote/     ← API, DTOs, Mappers
│   │   └── repository/ ← Implementations
│   ├── di/             ← Hilt modules
│   └── util/
└── ui/
    ├── screens/        ← Screens + ViewModels (theo feature)
    ├── components/     ← Shared Composables
    ├── navigation/     ← NavGraph, Routes
    └── theme/          ← Color, Type, Shape, Dimens
```

### 6.2 Repository pattern

```kotlin
// ✅ Interface trong domain/
interface IPlaceRepository {
    fun getPlaces(): Flow<List<Place>>
}

// ✅ Implementation trong data/
class PlaceRepository @Inject constructor(
    private val api: BeVietnamApi
) : IPlaceRepository {
    override fun getPlaces(): Flow<List<Place>> = flow {
        emit(api.getPlaces().map { it.toModel() })
    }
}

// ❌ Không bao giờ inject Repository trực tiếp vào ViewModel
// Phải qua UseCase
```

### 6.3 ViewModel state

```kotlin
// ✅ Sealed class cho UI state
sealed class ExploreUiState {
    object Loading : ExploreUiState()
    data class Success(val places: List<Place>) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
    object Empty : ExploreUiState()
}

// ✅ StateFlow, không dùng LiveData
val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
```

### 6.4 Composable naming

```kotlin
// ✅ PascalCase, không có "Composable" suffix
@Composable
fun PlaceCard(place: Place, onClick: () -> Unit) { ... }

// ✅ Preview function luôn đi kèm
@Preview(showBackground = true)
@Composable
fun PlaceCardPreview() {
    PlaceCard(
        place = Place(id = "preview", name = "Hội An", ...),
        onClick = {}
    )
}
```

### 6.5 Không hardcode data trong UI

```kotlin
// ❌ Hardcode string trong Composable
Text(text = "Khám Phá")

// ✅ Dùng string resource (i18n)
Text(text = stringResource(R.string.explore_title))
```

---

## 7. Viết Comment & Docstring

### 7.1 Khi nào cần comment

**Cần comment:**
- Logic phức tạp, không tự giải thích được qua code
- Giải thích lý do **TẠI SAO** (không phải **LÀM GÌ**)
- TODO/FIXME có context rõ ràng

**Không cần comment:**
- Code tự giải thích (`return user.is_active`)
- Lặp lại những gì code đã nói rõ

### 7.2 Format TODO

```python
# ✅ TODO với context đầy đủ
# TODO(Backend): Replace with PlaceRepository.get_all() after DB setup
# TODO(Sprint 2): Add caching layer for frequent queries

# ❌ TODO vô nghĩa
# TODO: fix this
# TODO: implement later
```

### 7.3 Docstring (Python)

```python
# ✅ Docstring cho public functions
async def create_capture(body: CaptureCreateRequest) -> CaptureResponse:
    """
    Create and persist a capture record.

    Sprint 1: stores in-memory.
    Sprint 2: persists to PostgreSQL via CaptureRepository.

    Args:
        body: Capture metadata from client.

    Returns:
        Created capture with generated ID and timestamp.
    """
```

---

## 8. Testing

### 8.1 Quy tắc tối thiểu

- Mỗi **UseCase / Service method** phải có ít nhất 1 unit test
- Mỗi **API endpoint** phải có ít nhất 1 integration test
- **Không** merge code mới mà không có test tương ứng (trừ UI-only changes)

### 8.2 Test naming

```python
# Python — format: test_<method>_<scenario>_<expected>
def test_create_capture_valid_body_returns_201():
def test_get_places_with_category_filter_returns_filtered():
def test_login_wrong_password_raises_401():
```

```kotlin
// Kotlin — format: `given_when_then` hoặc tương tự
@Test
fun `given valid credentials, when login, then returns JWT token`() { ... }

@Test
fun `given invalid place id, when get detail, then throws NotFoundException`() { ... }
```

### 8.3 Test structure (AAA pattern)

```python
def test_create_capture_valid_body_returns_capture():
    # Arrange
    body = CaptureCreateRequest(user_id="u1", place_id="p1")

    # Act
    result = await capture_service.create_capture(body)

    # Assert
    assert result.user_id == "u1"
    assert result.place_id == "p1"
    assert result.id is not None
```

---

## 9. Code Review Checklist

Trước khi request review, tự check:

### Chung
- [ ] Code compile/build không lỗi
- [ ] Không có `console.log`, `print()`, hay debug code
- [ ] Không có credentials hoặc API key hardcode
- [ ] Không có file `.env` trong commit

### Backend
- [ ] Mọi endpoint có `response_model` và `tags`
- [ ] Mọi function có type hints đầy đủ
- [ ] Không có business logic trong endpoint layer
- [ ] Error cases được handle bằng HTTPException

### Web
- [ ] Route file trong `app/` là thin wrapper (< 10 dòng)
- [ ] Components export named (không phải default)
- [ ] Import dùng `@/` alias, không dùng relative path
- [ ] Không có inline styles (trừ dynamic values)

### Mobile
- [ ] Không có hardcode string trong Composable
- [ ] ViewModel không inject Repository trực tiếp
- [ ] UI state dùng sealed class
- [ ] Preview function đi kèm mỗi Composable public

---

## 10. Những điều tuyệt đối KHÔNG làm

```
❌ Commit trực tiếp vào main branch
❌ Merge PR của chính mình khi chưa được review
❌ Push API keys, passwords, .env files lên Git
❌ Để file rỗng với comment "// implement later" mà không có stub
❌ Import mock/test data vào production code
❌ Dùng any type (TypeScript) mà không có lý do và comment
❌ Catch exception và bỏ qua (silent failure)
❌ Hardcode localhost URLs trong code
❌ Xóa test vì test bị fail thay vì sửa code
❌ Copy-paste code > 10 dòng mà không refactor thành function chung
```

---

## Thay đổi tài liệu này

Muốn thay đổi/bổ sung quy tắc:
1. Tạo PR với tiêu đề `docs(standards): <mô tả thay đổi>`
2. Discuss trong PR comments
3. Cần **ít nhất 2 thành viên** đồng ý trước khi merge

---

*"Clean code is not written by following a set of rules. You don't become a software craftsman by learning a list of heuristics. Professionalism and craftsmanship come from values that drive disciplines."*  
— Robert C. Martin
