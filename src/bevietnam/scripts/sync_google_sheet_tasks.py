#!/usr/bin/env python3
"""Sync Agile kickoff tasks to the Google Sheet task tab.

Run once without --apply to preview rows. Run with --apply to write.
"""

from __future__ import annotations

import argparse
import datetime as dt
from pathlib import Path
from typing import Any, Protocol


class SheetCredentials(Protocol):
    valid: bool
    expired: bool
    refresh_token: str | None

    def refresh(self, request: Any) -> None: ...

    def to_json(self) -> str: ...

SHEET_ID = "13ZkNQR33vm0j6GPp331DX1Bv6Z9QFELG3OtMEz-Pc00"
WORKSHEET_NAME = "task"
CLIENT_SECRET_PATH = Path(".secrets/client_secret.json")
TOKEN_PATH = Path(".secrets/google_sheets_token.json")
SCOPES = ["https://www.googleapis.com/auth/spreadsheets"]
HEADERS = ["Task", "Priority", "Owner", "Email", "Status", "Start date", "End date", "Detail", "Deliverable", "Notes"]
WRITE_START_ROW = 29

START_DATE = dt.date(2026, 5, 4)


def date(days_after_start: int) -> str:
    return (START_DATE + dt.timedelta(days=days_after_start)).strftime("%d/%m/%Y")


TASK_ROWS: list[dict[str, Any]] = [
    {
        "task": "Sprint 0: Set up Next.js website project shell",
        "priority": "P0",
        "owner": "website",
        "status": "Not started",
        "start": date(0),
        "end": date(3),
        "detail": "Set up the Next.js + TypeScript website shell, routing structure, base layout, theme/styling approach, and responsive page container. Acceptance criteria: web app runs locally, has placeholder pages, and is ready for API integration.",
        "deliverable": False,
        "notes": "Website role owns responsive web/PWA for desktop and iPhone browser users.",
        "mail": "",
    },
    {
        "task": "Sprint 0: Create website API client and mock data layer",
        "priority": "P0",
        "owner": "website",
        "status": "Not started",
        "start": date(1),
        "end": date(4),
        "detail": "Create a website API client structure and temporary mock data for profile, places, feed, storyline task, and vlogs. Acceptance criteria: pages can switch from mock data to FastAPI endpoints without rewriting page UI.",
        "deliverable": False,
        "notes": "Use Backend Engineer 2's API contracts as soon as available.",
        "mail": "",
    },
    {
        "task": "Sprint 0: Draft responsive discovery page",
        "priority": "P0",
        "owner": "website",
        "status": "Not started",
        "start": date(1),
        "end": date(5),
        "detail": "Build responsive discovery page using mock places. Acceptance criteria: page shows place cards with name, category, description, and basic location; layout works on desktop and mobile browser widths.",
        "deliverable": False,
        "notes": "Map can be placeholder/list-first if map integration is not ready.",
        "mail": "",
    },
    {
        "task": "Sprint 0: Draft responsive feed page with recommendation explanations",
        "priority": "P1",
        "owner": "website",
        "status": "Not started",
        "start": date(2),
        "end": date(6),
        "detail": "Build feed page using mock ranked recommendations. Acceptance criteria: each card shows recommended place, reason/explanation text, and simple ranking indicator so users understand why the item appears.",
        "deliverable": False,
        "notes": "This supports the project's context-aware recommendation value proposition.",
        "mail": "",
    },
    {
        "task": "Sprint 0: Draft vlog/memory viewing page",
        "priority": "P1",
        "owner": "website",
        "status": "Not started",
        "start": date(3),
        "end": date(7),
        "detail": "Build a generated vlog/memory page using mock post data. Acceptance criteria: page displays title, summary, body/content, date, and empty state when no generated memory exists.",
        "deliverable": False,
        "notes": "Can become the first visible AI output surface for demos.",
        "mail": "",
    },
    {
        "task": "Sprint 0: Check mobile browser responsiveness for iPhone users",
        "priority": "P1",
        "owner": "website",
        "status": "Not started",
        "start": date(4),
        "end": date(7),
        "detail": "Review main website pages at mobile browser widths because native iOS is not in MVP. Acceptance criteria: discovery, feed, and vlog pages are readable and usable on small screens.",
        "deliverable": False,
        "notes": "This is the MVP substitute for native iOS access.",
        "mail": "",
    },
    {
        "task": "Sprint 1: Connect website to backend health and places endpoints",
        "priority": "P0",
        "owner": "website",
        "status": "Not started",
        "start": date(5),
        "end": date(8),
        "detail": "Connect website API client to backend health check and GET /places once available. Acceptance criteria: website can show backend availability and render backend-provided places instead of hardcoded page-local data.",
        "deliverable": False,
        "notes": "Part of Sprint 1 vertical skeleton demo.",
        "mail": "",
    },
    {
        "task": "Sprint 1: Connect website feed and storyline surfaces to backend mocks",
        "priority": "P0",
        "owner": "website",
        "status": "Not started",
        "start": date(7),
        "end": date(10),
        "detail": "Connect feed page to GET /feed and a simple task/storyline surface to GET /storyline/next-task if included in web demo. Acceptance criteria: website renders backend mock recommendation and task data.",
        "deliverable": False,
        "notes": "Coordinate response shape with Backend Engineer 2.",
        "mail": "",
    },
    {
        "task": "Sprint 1: Prepare website vertical skeleton demo flow",
        "priority": "P0",
        "owner": "website",
        "status": "Not started",
        "start": date(8),
        "end": date(10),
        "detail": "Prepare a demo flow: open website, verify backend connection, view discovery places, view feed recommendation, and view mock/generated vlog page. Acceptance criteria: team can demo the website flow at Sprint 1 review.",
        "deliverable": False,
        "notes": "Focus on integrated flow before visual polish.",
        "mail": "",
    },
]


def get_credentials() -> SheetCredentials:
    try:
        from google.auth.transport.requests import Request
        from google.oauth2.credentials import Credentials
        from google_auth_oauthlib.flow import InstalledAppFlow
    except ModuleNotFoundError as exc:
        raise SystemExit(
            "Missing Google API dependencies. Install them with: "
            "python -m pip install -r scripts/requirements-google-sheets.txt"
        ) from exc

    creds = None
    if TOKEN_PATH.exists():
        creds = Credentials.from_authorized_user_file(str(TOKEN_PATH), SCOPES)

    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            flow = InstalledAppFlow.from_client_secrets_file(str(CLIENT_SECRET_PATH), SCOPES)
            creds = flow.run_local_server(port=0)
        TOKEN_PATH.write_text(creds.to_json())

    return creds


def make_values() -> list[list[Any]]:
    return [
        [
            row["task"],
            row["priority"],
            row["owner"],
            row["mail"],
            row["status"],
            row["start"],
            row["end"],
            row["detail"],
            row["deliverable"],
            row["notes"],
        ]
        for row in TASK_ROWS
    ]


def print_preview(values: list[list[Any]]) -> None:
    print("Preview rows to write:")
    print(" | ".join(HEADERS))
    print("-" * 160)
    for row in values:
        print(" | ".join(str(value) for value in row))


def write_sheet(values: list[list[Any]]) -> None:
    try:
        from googleapiclient.discovery import build
    except ModuleNotFoundError as exc:
        raise SystemExit(
            "Missing Google API dependencies. Install them with: "
            "python -m pip install -r scripts/requirements-google-sheets.txt"
        ) from exc

    creds = get_credentials()
    service = build("sheets", "v4", credentials=creds)
    header_range = f"{WORKSHEET_NAME}!A1:J1"
    service.spreadsheets().values().update(
        spreadsheetId=SHEET_ID,
        range=header_range,
        valueInputOption="USER_ENTERED",
        body={"values": [HEADERS]},
    ).execute()

    end_row = WRITE_START_ROW + len(values) - 1
    range_name = f"{WORKSHEET_NAME}!A{WRITE_START_ROW}:J{end_row}"
    body = {"values": values}
    service.spreadsheets().values().update(
        spreadsheetId=SHEET_ID,
        range=range_name,
        valueInputOption="USER_ENTERED",
        body=body,
    ).execute()
    print(f"Wrote headers to {header_range}")
    print(f"Wrote {len(values)} rows to {range_name}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply", action="store_true", help="Write rows to Google Sheets")
    args = parser.parse_args()

    values = make_values()
    print_preview(values)

    if args.apply:
        write_sheet(values)
    else:
        print("\nDry run only. Re-run with --apply to write to Google Sheets.")


if __name__ == "__main__":
    main()
