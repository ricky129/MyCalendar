        function initMap() {
            // Log the container size for debugging
            var mapDiv = document.getElementById('map');
            console.log('Map container size: ' + mapDiv.offsetWidth + 'x' + mapDiv.offsetHeight);

            if (mapDiv.offsetWidth === 0 || mapDiv.offsetHeight === 0) {
                console.log('Container size is 0, delaying initialization');
                setTimeout(initMap, 100);
                return;
            }

            var map = L.map('map', {
                center: [51.505, -0.09], // Default: London
                zoom: 13,
                zoomControl: true
            });
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
                tileSize: 256,
                zoomOffset: 0
            }).addTo(map);

            // Add a marker
            var marker = L.marker([51.505, -0.09]).addTo(map);

            // Ensure the map adjusts to the container size
            setTimeout(function() {
                map.invalidateSize();
                console.log('Map invalidated size after init');
            }, 100);

            map.on('click', function(e) {
                if (marker) {
                    marker.setLatLng(e.latlng);
                } else {
                    marker = L.marker(e.latlng).addTo(map);
                }
                window.javaCallback.setCoordinates(e.latlng.lat, e.latlng.lng);
            });

            map.on('zoomend', function() {
                map.invalidateSize();
                console.log('Map invalidated size after zoom');
            });

            window.onresize = function() {
                setTimeout(function() {
                    map.invalidateSize();
                    console.log('Map invalidated size after window resize');
                }, 100);
            };
        }

        // Delay initialization until the window is fully loaded and the container is sized
        window.onload = function() {
            setTimeout(initMap, 500); // Delay to ensure dialog is visible
        };