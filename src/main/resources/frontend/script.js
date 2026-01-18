const button = document.getElementById("submitBtn");
const resultElem = document.getElementById("result");

// Initialize Leaflet map
const map = L.map('map').setView([55.94468066708487, -3.1863580788986368], 15); // default center

// Add OpenStreetMap tiles
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
}).addTo(map);

// Layer group to hold delivery paths
let pathLayer = L.layerGroup().addTo(map);

button.addEventListener("click", async () => {
    const input = document.getElementById("dispatchInput").value;

    try {
        const response = await fetch("/api/v1/calcDeliveryPath", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: input
        });

        if (!response.ok) {
            resultElem.textContent = `Error: ${response.status} ${response.statusText}`;
            return;
        }

        const data = await response.json();
        resultElem.textContent = JSON.stringify(data, null, 2);

        // Clear previous paths
        pathLayer.clearLayers();

        // Loop through all drone paths
        if (data.dronePaths) {
            data.dronePaths.forEach(dronePath => {
                dronePath.deliveries.forEach(delivery => {
                    const coords = delivery.flightPath.map(p => [p.lat, p.lng]);
                    L.polyline(coords, { color: 'blue' }).addTo(pathLayer);

                    // Optional: mark start/end of each delivery
                    if (coords.length > 0) {
                        L.circleMarker(coords[0], { color: 'green', radius: 4 }).addTo(pathLayer);
                        L.circleMarker(coords[coords.length - 1], { color: 'red', radius: 4 }).addTo(pathLayer);
                    }
                });
            });

            // Collect all LatLngs from polylines and markers
            const allCoords = [];

            pathLayer.eachLayer(layer => {
                if (typeof layer.getLatLngs === 'function') {
                    // Polyline or polygon
                    allCoords.push(...layer.getLatLngs());
                } else if (typeof layer.getLatLng === 'function') {
                    // Marker or circleMarker
                    allCoords.push(layer.getLatLng());
                }
            });

            // Only fit bounds if we have coordinates
            if (allCoords.length > 0) {
                map.fitBounds(allCoords);
            }
        }

    } catch (err) {
        console.error(err);
        resultElem.textContent = "Error: " + err;
    }
});