# Software Requirements Specification

## 1. Functional Requirements (FR)

### 1.1 Geometric Core (Unit Level)
* **REQ-GEO-01:** Calculate the Euclidean distance between two geographic coordinates.
    * **Endpoint:** `POST /api/v1/distanceTo`
* **REQ-GEO-02:** Determine if two positions are "close" based on a distance threshold (< 0.00015 degrees).
    * **Endpoint:** `POST /api/v1/isCloseTo`
* **REQ-GEO-03:** Calculate the next GPS position given a start point and a specific compass angle.
    * **Endpoint:** `POST /api/v1/nextPosition`
* **REQ-GEO-04:** Detect if a specific coordinate falls within a defined polygonal region (Restricted Area).
    * **Endpoint:** `POST /api/v1/isInRegion`
* **REQ-GEO-05:** Validate that flight angles are strictly multiples of 22.5 degrees.
    * **Triggered by:** `POST /api/v1/nextPosition` (Internal validation logic)
* **REQ-GEO-06:** Validate that latitude and longitude inputs are within valid global ranges (-90 to 90, -180 to 180).
    * **Triggered by:** All Geometric Endpoints (Internal validation logic)

### 1.2 API Interface (Integration Level)
* **REQ-API-01:** Expose REST endpoints to accept JSON payloads for geometric calculations.
    * **Endpoints:** `/api/v1/distanceTo`, `/api/v1/isInRegion`, etc.
* **REQ-API-02:** Expose REST endpoints to receive dispatch requests and return calculated flight paths.
    * **Endpoint:** `POST /api/v1/calcDeliveryPath`
* **REQ-API-03:** Handle invalid JSON or malformed requests with appropriate HTTP 400 Bad Request status codes.
    * **Applies to:** All Endpoints (Controller Advice/Spring Validation).
* **REQ-API-04:** Provide a unique student identifier endpoint for identification.
    * **Endpoint:** `GET /api/v1/uid`

### 1.3 Drone Logistics & Navigation (System Level)
* **REQ-LOG-01:** Fetch live drone, service point, and restricted area data from the external ILP REST Service.
    * **Internal Dependency:** Triggered during `POST /api/v1/calcDeliveryPath` (via `IlpClient`).
* **REQ-LOG-02:** Filter available drones based on requested capabilities (Cooling, Heating, Capacity).
    * **Endpoint:** `GET /api/v1/dronesWithCooling/{state}` OR `POST /api/v1/query`.
* **REQ-LOG-03:** Verify drone availability for a specific time slot at a specific service point.
    * **Endpoint:** `POST /api/v1/queryAvailableDrones`.
* **REQ-LOG-04:** Calculate the optimal flight path using the A* algorithm, ensuring the path avoids all restricted areas.
    * **Endpoint:** `POST /api/v1/calcDeliveryPath`.
* **REQ-LOG-05:** Ensure the calculated flight path does not exceed the drone's maximum battery life (`maxMoves`).
    * **Triggered by:** `POST /api/v1/calcDeliveryPath` (Internal constraints).
* **REQ-LOG-06:** Calculate the total delivery cost based on initial cost, final cost, and cost per move.
    * **Triggered by:** `POST /api/v1/calcDeliveryPath` (Internal calculation).
* **REQ-LOG-07:** Generate path data in GeoJSON format for frontend map visualization.
    * **Endpoint:** `POST /api/v1/calcDeliveryPathAsGeoJson`.


## 2. Non-Functional Requirements (NFR)

### 2.1 Measurable Quality Attributes (Unit/System Level)
* **REQ-NFR-01 (Precision):** Geometric calculations must maintain floating-point accuracy with an epsilon tolerance of `1e-9` to prevent navigation drift.
* **REQ-NFR-02 (Grid Constraints):** All drone movements must strictly align to a grid step size of `0.00015` degrees (approx. 16 meters) to match the external map standard.
* **REQ-NFR-03 (Navigation Constraints):** Flight headings must be restricted to exactly 16 discrete compass directions (multiples of 22.5 degrees).
* **REQ-NFR-04 (Algorithmic Performance):** The pathfinding algorithm (A*) must cap node expansions (e.g., at 50,000 iterations) to prevent infinite loops or excessive memory usage during complex route calculations.

### 2.2 Qualitative Requirements (Integration/System Level)
* **REQ-NFR-05 (Robustness):** The API must handle invalid inputs (e.g., malformed JSON, missing fields) gracefully by returning standard HTTP `400 Bad Request` responses instead of crashing or exposing stack traces.
* **REQ-NFR-06 (Interoperability):** The system must effectively integrate with the third-party ILP REST Service to fetch dynamic data (Drones, Restricted Areas) and tolerate data format consistency.
* **REQ-NFR-07 (Standards Compliance):** The system must provide flight path data in the standard **GeoJSON** format (`LineString` feature) to ensure compatibility with standard mapping frontend libraries like Leaflet.js.
* **REQ-NFR-08 (Testability):** The application design should allow for the mocking of external dependencies (ILP Service) to facilitate deterministic regression testing (Note: This is an identified area for improvement).