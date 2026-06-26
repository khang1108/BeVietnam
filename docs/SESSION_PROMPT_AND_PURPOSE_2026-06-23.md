# Session Prompt And Purpose

Date: 2026-06-23

## Prompt Trich Xuat Tu Session

Session nay duoc hinh thanh tu chuoi yeu cau sau:

1. Nguoi dung dang xay dung ung dung mobile + web cho tourist, tap trung vao Vietnam culture.
2. Can viet va refine spec de tiep tuc hoan thien project, huong toi business case that.
3. MVP da co, trong 8-10 ngay can chay pilot voi tourist that va launch usable product.
4. Team 5 nguoi, giao dien web/mobile dang duoc teammate phat trien, phan trong tam la backend + AI agent service.
5. San pham can:
   - hien thi feed goi y dia diem theo kieu Facebook feed,
   - moi post de xuat dia diem van hoa phu hop voi vi tri hien tai,
   - dua tren weather, traffic, culture information,
   - co bubble map voi kich thuoc bubble thay doi theo dieu kien hien tai,
   - co storyline/game kieu geocaching voi task check-in/capture.
6. User khong muon plan chi tiet theo tung moc gio; user muon recommendation feed + explainable scoring.
7. Du lieu van hoa can duoc curate thanh knowledge base tu cultural facts va ebook, uu tien nguon chinh thuc cua co quan nha nuoc va UNESCO.
8. Stack da chot:
   - OpenWeather API,
   - Google API cho traffic/travel context,
   - Goong cho map,
   - Firebase cho auth/storage,
   - PostgreSQL cho product data,
   - Qdrant cho vector retrieval,
   - Gemini + LangGraph cho AI service.
9. User yeu cau tap trung AI Service truoc, backend se lam sau.

## Muc Dich Cua Session

Muc dich chinh cua session nay:

- chot huong kien truc AI service cho pilot Hue,
- bien spec thanh cac task co the implement duoc,
- thuc hien cac task nen tang cho AI service de du an co the tiep tuc nhanh.

Cu the, session nay nham:

1. Xuat tai lieu de team co the dung ngay:
   - `docs/PROJECT_SPEC.md`
   - `docs/HUE_PILOT_8_DAY_DELIVERY_PLAN.md`
   - `docs/AI_SERVICE_IMPLEMENTATION_PLAN.md`
2. Hoan thanh cac task AI service ban dau:
   - Task 1: make AI service runnable
   - Task 2: define knowledge chunk schema
   - Task 3: rebuild CultureScout retrieval
3. Chot cac nguyen tac van hanh:
   - AI chi generate sau khi retrieval,
   - claim van hoa phai co source metadata,
   - fallback phai hoat dong khi Gemini/Qdrant/provider loi,
   - Firebase dung cho auth + media,
   - PostgreSQL la source of truth cho product data,
   - Qdrant la source of truth cho retrieval vectors.

## Ket Qua Chinh Cua Session

- AI service import path da duoc sua theo monorepo hien tai.
- Docker entrypoint cua AI service da duoc cap nhat.
- Knowledge chunk schema da duoc dinh nghia de phuc vu traceability.
- CultureScout da ho tro retrieval theo:
  - `place_name`
  - `place_id`
  - `category`
  - `language`
  - `interests`
- Fallback facts da duoc bo sung source metadata thay vi chi de `source=fallback`.
- AI service plan da duoc cap nhat theo quyet dinh dung:
  - Firebase cho auth/storage
  - Managed PostgreSQL cho product database
  - Qdrant cho knowledge retrieval

## Huong Tiep Theo

Task tiep theo duoc xac dinh trong session:

- Task 4: Implement AI scoring and grounded recommendation explanation.

Task nay se thay the phan `TripAdvisorAgent` dang la stub bang:

- `culture_score`
- `suitability_score`
- `bubble_size`
- `reason_codes`
- `cultural_highlight`
- `source_refs`
- fallback explanation khi Gemini that bai

## Update Bo Sung Ve Prompt Van Hanh

Muc nay duoc them sau, de luu lai prompt van hanh hieu luc cua agent trong phien thao tac nay ma khong ghi de len session cu.

### Muc Dich Cua Prompt Van Hanh

- Hoat dong nhu mot coding agent thuc dung, uu tien hoan thanh task den cuoi trong workspace hien tai.
- Doc codebase truoc khi ket luan, uu tien thay doi nho, dung pattern san co cua repo.
- Khi co the, thuc hien trien khai thuc te thay vi chi dua ra de xuat.
- Bao toan thay doi co san trong worktree, khong revert file cua user neu khong duoc yeu cau.
- Giao tiep ngan gon, cap nhat tien do ro rang, va kiem chung ket qua neu co the.

### Prompt Hieu Luc Cua Agent

1. Vai tro:
   - La coding agent lam viec truc tiep trong workspace.
   - Muc tieu la giai quyet task end-to-end, khong dung lai o phan tich neu van co the tiep tuc.
2. Cach thao tac:
   - Uu tien dung `rg` de tim file/noi dung.
   - Uu tien doc repo truoc, khong doan mo ho.
   - Co the chay lenh, doc file, sua file, va xac minh ket qua trong workspace.
3. Nguyen tac sua file:
   - Dung `apply_patch` cho sua file thu cong.
   - Mac dinh dung ASCII.
   - Khong them comment thua.
   - Khong dung lenh pha huy nhu `git reset --hard` neu khong co yeu cau ro rang.
4. Nguyen tac lam viec voi git:
   - Chap nhan worktree co the dang dirty.
   - Khong revert thay doi khong phai do agent tao ra.
   - Neu co xung dot truc tiep voi task hien tai thi can doc ky va lam viec cung thay doi do.
5. Cach phan hoi:
   - Cap nhat trung gian ngan gon trong luc dang lam.
   - Cau tra loi cuoi cung phai uu tien ket qua, xac minh, va rui ro con lai.
6. Dinh huong ky thuat:
   - Uu tien giai phap bao thu, phu hop pattern san co.
   - Neu task lien quan frontend thi giu tinh nhat quan voi design system hien co.
   - Neu user yeu cau review thi uu tien tim bug, regression risk, va test gap.

### Ghi Chu

- Phan tren la ban export cua prompt van hanh o muc y nghia va hanh vi.
- Khong bao gom nguyen van toan bo metadata noi bo va tool schema, vi phan do dai va khong huu ich cho tai lieu du an.

## Update Bo Sung Ve Prompt Va Muc Dich Hien Tai

Phan nay cap nhat them de phan anh task ky thuat dang duoc uu tien trong repo hien tai.

### Prompt Ky Thuat Hien Tai

Session hien tai duoc day boi yeu cau sau:

1. Tich hop weather that tu OpenWeather theo GPS that cua trinh duyet.
2. API key OpenWeather phai duoc giu o backend; frontend chi gui `latitude` va `longitude`.
3. Khong dung demo-location fallback:
   - neu browser bi tu choi quyen geolocation, storyline quay ve luong AI generation fallback san co;
   - explore van giu mock weather lam gia tri khoi tao cho den khi lay duoc weather that.
4. Pham vi ap dung weather that gom hai be mat:
   - `storyline` de anh huong toi question pool scoring;
   - `explore` de anh huong toi bubble sizing va weather indicator tren UI.
5. Huong trien khai uu tien:
   - tao weather service dung chung o backend;
   - them endpoint weather cho frontend explore batch-call;
   - tai su dung scoring rule hien co, khong viet lai co che score.

### Muc Dich Hien Tai

Muc dich cua dot cap nhat nay la bien weather tu mock/fallback thanh mot context that, co kiem soat, va phuc vu dung hai hanh vi san pham:

1. `Storyline` khong con score theo weather gia lap khi co GPS that.
2. `Explore` co the co gian bubble theo thoi tiet that cua tung cum dia diem.
3. Khoa bi mat `OPENWEATHER_API_KEY` o backend, tranh lo key tren web client.
4. Duy tri fallback an toan de UX khong vo:
   - storyline van chay duoc neu user tu choi GPS;
   - backend weather service co the tra `any` khi chua cau hinh API key;
   - cache TTL giam so lan goi OpenWeather va tranh phat sinh quota khong can thiet.

### Ket Qua Tai Lieu Can Phan Anh

Neu team tiep tuc cap nhat docs theo thay doi code, thi session nay can duoc hieu la da chot cac quyet dinh sau:

- `OpenWeather` la nguon weather runtime chinh thuc.
- `Browser geolocation` la nguon toa do that duy nhat cho flow weather tren frontend.
- `Backend weather service` la lop trung gian duy nhat duoc quyen goi OpenWeather.
- `Question pool scoring` giu nguyen rule cu, chi bo sung context weather that trong `resolve_context`.
- `Explore` dung endpoint batch weather de tranh moi marker tu goi rieng le.

### Tac Dong Den Kien Truc

- Backend can co them `weather_service`, schema weather, va router weather.
- Frontend can mo rong API client de gui GPS cho `storyline` va batch-fetch weather cho `explore`.
- Cac docs/bao cao lien quan den AI prompt va product purpose nen mo ta ro rang rang weather that duoc dua vao de phuc vu `recommendation usefulness` va `guided exploration`, khong chi de hien thi thong tin phu.

## Session 23/06 — Hoan Thien Bao Cao, Slide, Van Hanh

Day du chuoi prompt cua phien lam viec ngay 23/06/2026, theo thu tu thoi gian.
Day la giai doan hoan thien (bao cao LaTeX, slide thuyet trinh, va go loi van hanh tren VM GPU),
sau khi he thong da chay duoc.

| # | Prompt (nguyen van / rut gon) | Muc dich | Ket qua |
|---|-------------------------------|----------|---------|
| 1 | "o tong quan he thong, hay ve mot graph trinh bay ve pipeline he thong... chia ro backend layer, frontend layer, vllm layer" | Lam ro kien truc tong the bang so do phan lop | So do 3 lop Frontend/Backend/vLLM trong muc Tong quan he thong (`fig:system_pipeline`) |
| 2 | "ban tu update nguon giup toi di" | Moi so lieu phai co nguon that, khong bia | Xac minh qua web 4 thong ke du lich (GSO, Cuc Du lich QG, Bo VHTTDL, PATA qua Ha Noi Moi); sua entry `ref.bib` tro URL that |
| 3 | "please commit all changes" | Luu thay doi dang theo doi | Commit go report CT cu, refresh book markdown |
| 4 | "commit features also" | Theo doi luon report (dang bi gitignore) | Un-ignore `docs/report/`, commit source + pdf + so do + citation |
| 5 | "push to main" | Phat hanh len nhanh chinh | Fast-forward `main`, da push |
| 6 | "dua vao data/ ... them pseudo code chua file json mau trong cac feature cua section 3.3" | Minh hoa schema bang du lieu that | 6 listing JSON lay tu `data/` (chunk, post, question, nearby, capture, bilingual) |
| 7 | "hay dich cai vLLM layer xuong mot chut... no dang bi dinh voi layer backend" | Sua bo cuc so do | Ha node vLLM xuong, tach band khoi Backend |
| 8 | "trong phan Trien khai... noi ro da trien khai o dau... Web App -> Azure, Backend + vLLM -> Thundercompute L40" | Khai bao ha tang chay that | Muc Trien khai: prose + nhan so do + bang `tab:infra` (Azure / Thundercompute L40 / Cloudflare) |
| 9 | "lay slide style tu FactChecking... lam 10-15 slides, han che chu, chu yeu keywords + diagram" | Dung bo slide thuyet trinh | Deck beamer `docs/slides/` (CambridgeUS + UFT palette) |
| 10 | "download this image... first image of slide 2" (x3 URL) | Dua anh pain-point that vao slide | Tai 3 anh vao `slides/img`, gan slide 2 |
| 11 | "add a slide to show the terminal of vllm working" | Minh chung vLLM chay | Slide terminal vLLM |
| 12 | "replace it to figure... named vllm_example.png" | De tu thay anh that sau | Doi slide vLLM sang figure placeholder co `\IfFileExists` |
| 13 | "o phan kien truc tong the 7.1 them figure bevietnam_homepage.jpg" + "them 1 slide truoc vllm dung homepage" | Dua anh san pham vao bao cao & slide | `fig:homepage` (7.1) + slide "San pham — Web App" |
| 14 | "added bevietnam detail map + map, them vao report va slides" | Minh hoa bubble map that | `fig:bubble_maps` (muc 3) + slide 9 dung 2 map |
| 15 | "chinh lai size cua image trong realtime bubble map, no bi vuot qua slide" | Sua tran khung slide | Doi 2 map sang canh nhau, cap `height` |
| 16 | "o dau bao cao them duong dan bevietnam.iamphuckhang.dev" | Gan live demo len bia | Hyperlink o trang tieu de |
| 17 | "loi khi chay setup.sh" (minio Permission denied -> Segmentation fault) | Khac phuc van hanh VM GPU | Chan: thieu `+x` roi binary hong -> chmod + tai lai; nguyen nhan ephemeral Thunder |
| 18 | "loi KV cache vLLM (0.26 GiB < 0.88 GiB)" | Chay duoc model tren L40 | Chan: VL profiling an VRAM; bump `gpu_memory_utilization` / giam `max_model_len` |
| 19 | "thieu phan trinh bay vllm_hosting (agent sinh knowledge)" | Bo sung khau sinh tri thuc offline | Muc moi + `fig:offline_pipeline` + `tab:offline_agents`; slide pipeline |
| 20 | "vua bo sung nhieu anh trong images/, them vao report va slide" | Dua anh mobile/storyline/vLLM vao | `fig:storyline`, `fig:mobile`, `fig:vllm_serve`; slide Mobile moi |
| 21 | "them muc trinh bay da prompt nhu nao, muc dich gi qua cac session" | Minh bach cach dung AI | Muc "Qua trinh Su dung Tro ly AI" trong bao cao + tai lieu nay |

### Nguyen Tac Prompt Xuyen Suot

- Neu ro muc tieu + boi canh trong moi prompt, khong yeu cau chung chung.
- Tuyet doi khong bia nguon: moi so lieu/trich dan phai truy duoc ve tai lieu that, neu khong thi bo.
- Vong lap kiem chung: AI de xuat -> nhom bien dich/chay thu/doc lai -> prompt sua. Nhom chiu trach nhiem cuoi cung.
- AI dung de tang toc thuc thi (sinh ma, so do, soan bao cao, go loi), khong thay nhom ra quyet dinh thiet ke.
