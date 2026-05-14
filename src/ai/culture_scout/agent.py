from ai.common.qdrant_store import QdrantStore


class CultureScout:
    def __init__(self, store: QdrantStore | None = None) -> None:
        self.store = store or QdrantStore()

    def retrieve(self, query: str) -> list[dict]:
        return self.store.search(query)
