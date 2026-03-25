# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I will refactor the data access layer to be more consistent. Right now, the codebase
uses three different strategies, and each has its trade-offs:

1. Panache entities (Store) - This approach is quick to write since the entity is tightly coupled
   to persistence. However, it becomes painful for unit testing because we can't easily mock
   Store.findById() without a real database. Also, persistence concerns leak into the REST layer,
   which isn't ideal.

2. PanacheRepository (Product) - This is better than Panache entities because you can mock the
   repository. But we are still passing JPA entities directly up to the REST handler, which means
   any database schema change will ripple through and affect the API contract.

3. Custom repository + port (Warehouse) - This follows the Hexagonal Architecture pattern. The
   domain model is independent of JPA, use cases only depend on port interfaces, and you can
   swap out the database adapter without touching business logic. It requires more boilerplate
   upfront, but it really pays off in testability and long-term maintainability.

If I were maintaining this codebase long-term, I will gradually move everything toward the Warehouse
pattern. I wouldn't do it in one big bang refactoring - instead,i will do it module by module
as we naturally touch that code for other work. This incremental approach minimizes risk and
spreads out the effort.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Both approaches work well in different situations, so I wouldn't pick just one for everything.

OpenAPI with Code Generation (Warehouse):

Advantages:
  - Single source of truth - the spec and implementation stay in sync automatically
  - You get documentation and client SDKs almost for free
  - Great when other teams or external clients need to consume your API
  - Type safety is baked in, which catches errors early

Disadvantages:
  - You're kind of stuck with what the generator gives you. For example, we had to write a custom
    filter just to handle HTTP 201 responses because the tool didn't support it
  - You need to nail down the spec upfront, which can slow you down when you're still figuring
    things out
  - Sometimes you end up fighting the generator instead of just writing the code you need

Hand-Coded (Store, Product):

Advantages:
  - Total control over everything - status codes, validation, error handling, whatever you need
  - Fast to iterate and prototype without worrying about spec files
  - No generator quirks or workarounds to deal with
  - You can make changes quickly without updating specs first

Disadvantages:
  - No machine-readable contract unless you manually maintain an OpenAPI spec
  - Documentation can easily drift from the actual implementation over time
  - Other teams consuming your API don't have a formal contract to work from

My take: Use OpenAPI for external APIs or anything shared with other teams where you want a
clear contract. For internal APIs where you're iterating quickly, hand-coding is fine. It really
depends on whether you need that formal contract or if speed and flexibility matter more.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
I'd prioritize tests based on what gives us the most confidence with the least maintenance burden.
Here's how I'd approach it:

1. Use Case / Business Logic Tests (Top Priority)
   Start here because this is where the actual business rules live - things like duplicate business
   unit codes, capacity limits, stock matching, and max warehouses per location. These tests are fast
   because you can mock the ports, so no database needed. If a capacity check breaks, we could end up
   overselling warehouse space, which is a real problem. I'd make sure to cover the happy path, each
   validation failure, and boundary cases like when capacity is exactly at the limit.

2. Integration / REST Tests (Second Priority)
   A handful of tests per resource using REST Assured, hitting the real HTTP layer. These catch wiring
   issues that unit tests miss - like the @Transactional annotation not binding through the proxy, which
   we've actually run into. I wouldn't test every single validation through HTTP though. Just the main
   flows and verify that error codes map correctly.

3. Repository Tests (Third Priority)
   Run a few tests against real Postgres using DevServices for custom queries like countActiveByLocation
   and totalCapacityByLocation. A typo in JPQL can silently return wrong data, so it's worth verifying
   these against an actual database. Keep it focused on complex queries, not basic CRUD.

4. CDI / Observer Tests (Fourth Priority)
   For things like the Store legacy sync, I'd test that the observer fires only after a successful
   commit and stays silent on rollback. These are easy to break during refactors, so having tests
   gives you confidence when touching that code.

How to keep coverage useful over time:
- The 80% JaCoCo gate in CI is already doing the heavy lifting - PRs can't drop coverage below that
  threshold, which prevents the slow erosion of test quality
- Name tests after the business rule they protect, not just the method name. For example,
  "shouldRejectWarehouseWhenCapacityExceedsLimit" instead of "testCreateWarehouse". This helps the
  next person understand why the test exists and whether it's still relevant
- Review coverage reports during PRs, but focus on whether critical paths are tested, not just
  hitting a percentage target
```