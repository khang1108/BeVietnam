from fastapi import APIRouter
from sqlalchemy import text
from app.core.database import engine

router = APIRouter()


@router.get("/dt", tags=["Debug"])
async def show_database_tables():
    """Return the list of public database tables and row counts."""
    async with engine.connect() as connection:
        table_result = await connection.execute(
            text(
                "SELECT table_name FROM information_schema.tables "
                "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' "
                "ORDER BY table_name"
            )
        )
        tables = [row[0] for row in table_result]

        counts = []
        for table_name in tables:
            count_result = await connection.execute(text(f'SELECT COUNT(*) FROM "{table_name}"'))
            counts.append({"table_name": table_name, "count": int(count_result.scalar_one())})

    return {"tables": counts}
