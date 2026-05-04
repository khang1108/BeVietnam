# Project Memory

Last updated: 2026-04-21

## Project Snapshot

- Project: Agent-Based Smart Tourism System for Vietnam
- Main analysis artifact: [PA_Decomposition.md](/home/phuckhang/MyWorkspace/TDTT/An-Agent-Based-Smart-Tourism-System-for-Vietnam/PA_Decomposition.md)
- Product direction: mobile + web application for tourism discovery in Vietnam

## Core Idea

The product helps travelers discover meaningful places in Vietnam by combining:

- trusted cultural knowledge from textbooks, official websites, and reputable news;
- contextual recommendation through personalized feed;
- bubble-based map visualization driven by weather, crowdedness proxies, reviews, and freshness;
- social travel behaviors such as check-ins, comments, and sharing moments with friends;
- hidden-gem discovery for alley food and local events that tourists usually miss.

## Current Analysis Decisions

- The report should stay anchored to stakeholder pain points, especially international tourists.
- `PA_Decomposition.md` is the main report for Problem Analysis and Decomposition.
- The report should not read as a flat checklist; it should include:
  - analytical prose between structured sections
  - cited market and policy evidence
  - mermaid diagrams for system understanding
  - explicit explanation of the Multi-Agent problem-solving approach
- Problem Analysis must explicitly cover:
  - Clarification Questions
  - Stakeholder Mapping
  - Requirements Elicitation
  - Constraint Specification
  - Use Cases
  - Epic User Stories
  - Formalization
- Decomposition must explicitly cover:
  - functional parts
  - clear input/output
  - loose coupling
  - coherence and single responsibility
  - reuse
  - alignment with user goals
  - flexibility
- Decomposition methods to reflect in the report:
  - functional decomposition
  - data-flow decomposition
  - event-based decomposition
  - scenario-based decomposition
  - constraint-driven decomposition

## Working Assumptions

- MVP should start with one locality before expanding nationwide.
- Primary target users are international travelers, without excluding domestic travelers.
- Bubble score should represent visit opportunity in current context, not raw popularity alone.
- Social features are important, but always subordinate to the tourism discovery value proposition.
- Cost awareness is required, but the project should not hard-code a zero-budget assumption unless the owner confirms it.

## Known Risks

- Source reliability and legal/TOS issues for data ingestion
- Cultural explanation quality and grounding
- Hidden-gem coverage versus trust
- Data freshness for events and opening status
- Privacy risks for location, photos, and social graph

## Sources Used in Current Report Revision

- [R1] VNAT official PDF `Thông tin du lịch tháng 12/2025`
- [R2] VTV article citing VNAT data on Q1 2025 international arrivals
- [R3] VNAT 65-year tourism portal article on digital transformation and synchronized smart tourism ecosystem
- [R4] VNAT rural tourism promotion page on Can Tho digitization targets for rural tourism
- [R5] VnEconomy article on fragmented tourism/hospitality data and decision-making
- [R6] VnEconomy summary of Booking.com 2025 travel behavior signals in Vietnam
- [R7] Thuong Gia article citing Booking.com event-travel and social inspiration signals
- [R8] VnEconomy report on Tet 2026 travel overload and short-term demand volatility

## Current Writing Direction

- Section `1.6 Pain Point Analysis` should be analytical prose, not short bullet enumeration.
- Each major pain point should connect:
  - observed user or ecosystem problem
  - supporting evidence or recent data
  - implication for system design

## Next Likely Steps

- Review and score the updated `PA_Decomposition.md` against the rubric
- Decide pilot locality and target user segment explicitly
- Convert this analysis into a solution architecture and implementation plan only after problem framing is accepted
