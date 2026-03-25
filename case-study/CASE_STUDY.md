# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking

**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Challenges and strategies:**

- **Challenge – Attribution:** Deciding how to attribute shared costs (e.g. transport, overhead) to a specific warehouse or store. **Strategy:** Define allocation rules (e.g. by volume, weight, or activity) and implement them in a cost engine; document assumptions and review periodically.
- **Challenge – Data quality and timeliness:** Costs from different systems (HR, logistics, finance) may be inconsistent or delayed. **Strategy:** Single source of truth where possible; clear ownership of master data; reconciliation checks and alerts when data is stale or inconsistent.
- **Considerations:** Granularity (per SKU vs per location vs per order), treatment of fixed vs variable costs, and how to handle reallocations when plans change. Key questions: Who owns cost master data? What is the required freshness for reporting (daily/weekly/monthly)?

---

## Scenario 2: Cost Optimization Strategies

**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Challenges and strategies:**

- **Identify:** Use current cost and operational data (e.g. cost per order, per unit, by warehouse/store) to find the largest cost buckets and outliers; benchmark against industry or internal best performers.
- **Prioritize:** Score initiatives by impact (savings, risk reduction) and effort (time, investment); involve operations and finance so trade-offs (e.g. service levels) are explicit.
- **Strategies (examples):** Consolidation of shipments or warehouses where service allows; slotting and picking path optimization to cut labor; better demand forecasting to reduce expedites and excess stock; automation where labor is a major cost.
- **Implementation:** Pilot in one region or segment; define KPIs (cost, service, safety); iterate and scale. Keep quality and service in the success criteria to avoid “cost cutting at any cost.”

---

## Scenario 3: Integration with Financial Systems

**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Challenges and strategies:**

- **Benefits:** Single view of costs for management; alignment between operational and financial reporting; faster close and audit trail; fewer manual re-entries and reconciliation.
- **Challenge – Consistency and semantics:** GL codes, cost centers, and calendars must align so that “cost” means the same in operations and finance. **Strategy:** Agree on a shared data model and mappings; document and version them; validate in UAT with finance.
- **Challenge – Real-time vs batch:** True real-time may be unnecessary and add complexity. **Strategy:** Define “timely” per use case (e.g. daily sync for reporting, near-real-time for alerts); use event-driven or scheduled sync with idempotency and retries; monitor latency and failure rates.
- **Considerations:** Security and access control, audit logging, and handling of corrections and restatements in the source system.

---

## Scenario 4: Budgeting and Forecasting

**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Challenges and strategies:**

- **Importance:** Enables planning (capacity, hiring, capex), early detection of variances, and accountability (budget owners). In fulfillment, volume and mix drive labor and transport, so forecasting demand and capacity is central.
- **Design considerations:** (1) **Drivers** – link costs to drivers (orders, units, shipments, headcount) so forecasts are explainable. (2) **Granularity** – by location, cost type, and time (month/quarter). (3) **Scenarios** – baseline, optimistic, pessimistic to stress-test. (4) **Rolling updates** – re-forecast periodically and compare to budget and prior forecast. (5) **Collaboration** – input from operations and finance; clear ownership of assumptions.
- **Challenges:** Volatility of demand and costs; bias (e.g. sandbagging). Mitigate with clear assumptions, review cycles, and tracking of forecast accuracy over time.

---

## Scenario 5: Cost Control in Warehouse Replacement

**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Challenges and strategies:**

- **Why preserve history:** Compliance and audit (explain past spend); comparison of old vs new site performance; learning (what drove cost in the old site); continuity of reporting (same BU code, but historical vs new period clearly separated).
- **Relation to budget:** The new warehouse should have its own budget and targets. Historical data helps set realistic targets (e.g. cost per unit at the old site) and track “before vs after” to validate that the replacement delivers expected savings without degrading service.
- **Implementation (aligned with this codebase):** Archive the old warehouse (soft delete / status) and create the new one with the same business unit code; in the data model, keep archived records with timestamps so cost history remains queryable by BU code and period. The Cost Control Tool should filter or segment by “active” vs “archived” and by date so reports and budgets refer to the right scope.

---

## Instructions for Candidates

Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
