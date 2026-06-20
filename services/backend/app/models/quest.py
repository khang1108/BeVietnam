# models/quest.py
from sqlalchemy import Boolean, Column, ForeignKey, Integer, String
from sqlalchemy.orm import relationship

from services.backend.app.core.database import Base

class QuestChain(Base):
    __tablename__ = "quest_chains"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    description = Column(String)
    tasks = relationship("QuestTask", back_populates="chain", cascade="all, delete-orphan")

class QuestTask(Base):
    __tablename__ = "quest_tasks"
    id = Column(Integer, primary_key=True, index=True)
    chain_id = Column(Integer, ForeignKey("quest_chains.id"))
    order_index = Column(Integer, nullable=False) # Dùng để xác định task tiếp theo
    title = Column(String, nullable=False)
    requirement = Column(String)
    
    chain = relationship("QuestChain", back_populates="tasks")

class UserProgress(Base):
    __tablename__ = "user_progress"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(String, ForeignKey("users.id"), index=True)
    chain_id = Column(Integer, ForeignKey("quest_chains.id"))
    current_task_id = Column(Integer, ForeignKey("quest_tasks.id"))
    is_completed = Column(Boolean, default=False)
