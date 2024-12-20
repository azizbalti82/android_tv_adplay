//domain of the server
var url = "https://adplayforandroidtv-production-13eb.up.railway.app"




// Function to fetch data and update the table
async function fetchDevices(section) {
    try {
        // Fetch data from the API
        const response = await fetch(url+'/devices');
        const devices = await response.json();

        // Get the table body element
        const tableBody = document.querySelector('#deviceTable tbody');
        
        // Clear existing rows (if any)
        tableBody.innerHTML = '';

        if(devices.length === 0){
            // If no devices, show the "Empty" row
            const emptyRow = document.createElement('tr');
            const emptyCell = document.createElement('td');
            emptyCell.colSpan = 7;  // Span across all columns
            emptyCell.style.textAlign = 'center';
            emptyCell.style.verticalAlign = 'middle';
            emptyCell.textContent = 'Empty';
            emptyRow.appendChild(emptyCell);
            tableBody.appendChild(emptyRow);
        }else{
            // Iterate over the devices and create table rows
            devices.forEach(device => {
                const row = document.createElement('tr');
                
                const name = document.createElement('td');
                name.textContent = device.name;
                row.appendChild(name);

                const createdAt = document.createElement('td');
                createdAt.textContent = formatDate(device.createdAt);
                row.appendChild(createdAt);

                const lastSeen = document.createElement('td');
                lastSeen.textContent = formatDate(device.lastSeen) || 'N/A';
                row.appendChild(lastSeen);

                const status = document.createElement('td');
                if(device.status == 'online'){
                    status.style.color = "green";
                }else{
                    status.style.color = "red";
                }
                status.textContent = device.status;
                row.appendChild(status);


                const tools = document.createElement('td');
                // Edit Button
                const editButton = document.createElement('button');
                editButton.textContent = 'Edit';
                editButton.onclick = () => editDeviceToggleForm(device); // Edit functionality
                editButton.classList.add('btn', 'btn-primary');
                editButton.style.marginRight = '5px'
                // Delete Button
                const deleteButton = document.createElement('button');
                deleteButton.textContent = 'Delete';
                deleteButton.onclick = () => deleteDevice(device.id); // Delete functionality
                deleteButton.classList.add('btn', 'btn-danger');
                // Add buttons to the tools cell
                tools.appendChild(editButton);
                tools.appendChild(deleteButton);
                // Append the tools cell to the row
                row.appendChild(tools);
                

                // Append the row to the table
                tableBody.appendChild(row);
            });
        }
        
    } catch (error) {
        console.error('Error fetching data:', error);
    }
}
async function fetchAds(section) {
    try {
        // Fetch data from the API
        const response = await fetch(url+'/ads');
        const devices = await response.json();

        // Get the table body element
        const tableBody = document.querySelector('#adTable tbody');
        
        // Clear existing rows (if any)
        tableBody.innerHTML = '';

        if(devices.length === 0){
            // If no devices, show the "Empty" row
            const emptyRow = document.createElement('tr');
            const emptyCell = document.createElement('td');
            emptyCell.colSpan = 6;  // Span across all columns
            emptyCell.style.textAlign = 'center';
            emptyCell.style.verticalAlign = 'middle';
            emptyCell.textContent = 'Empty';
            emptyRow.appendChild(emptyCell);
            tableBody.appendChild(emptyRow);
        }else{
            // Iterate over the devices and create table rows
            devices.forEach(device => {
                const row = document.createElement('tr');
                
                const title = document.createElement('td');
                title.textContent = device.title;
                row.appendChild(title);

                const description = document.createElement('td');
                description.textContent = device.description || 'N/A';
                row.appendChild(description);

                const createdAt = document.createElement('td');
                createdAt.textContent = formatDate(device.createdAt);
                row.appendChild(createdAt);

                const type = document.createElement('td');
                type.textContent = device.type;
                row.appendChild(type);

                const mediaUrl = document.createElement('td');
                const link = document.createElement('a');
                link.href = "media/"+device.id;
                link.textContent = "Preview";
                link.target = '_blank';
                mediaUrl.appendChild(link);
                row.appendChild(mediaUrl);


                const tools = document.createElement('td');
                // Edit Button
                const editButton = document.createElement('button');
                editButton.textContent = 'Edit';
                editButton.onclick = () => editAdToggleForm(device); // Edit functionality
                editButton.classList.add('btn', 'btn-primary');
                editButton.style.marginRight = '5px'
                // Delete Button
                const deleteButton = document.createElement('button');
                deleteButton.textContent = 'Delete';
                deleteButton.onclick = () => deleteAd(device.id); // Delete functionality
                deleteButton.classList.add('btn', 'btn-danger');
                // Add buttons to the tools cell
                tools.appendChild(editButton);
                tools.appendChild(deleteButton);
                // Append the tools cell to the row
                row.appendChild(tools);
                

                // Append the row to the table
                tableBody.appendChild(row);
            });
        }
        
    } catch (error) {
        console.error('Error fetching data:', error);
    }
}
/* this is an old working version, but i had problem with duplications in the table (nnot in the server) that is why i commented it out
async function fetchSchedules() {
    const tableBody = document.querySelector('#scheduleTable tbody');
    let count_fetched = 0;
    try {
        // Clear existing rows (if any)
        tableBody.innerHTML = '';

        // Fetch data from the API
        const response = await fetch(url+'/schedules');
        const result = await response.json();

        // Check if there are any devices
        if (result.length > 0) {
            // Iterate over the devices and create table rows
            for (const device of result) {
                const row = document.createElement('tr');

                try {
                    // Fetch additional details for the device and ad
                    const deviceResponse = await fetch(`url+'/devices/${device.device_id}`);
                    const deviceData = await deviceResponse.json();

                    const adResponse = await fetch(`url+'/ads/${device.ad_id}`);
                    const adData = await adResponse.json();
                    
                    // Populate the table row
                    const ad_id = document.createElement('td');
                    ad_id.textContent = adData.ad.title;
                    row.appendChild(ad_id);
                

                    const device_id = document.createElement('td');
                    device_id.textContent = deviceData.Device.name;
                    row.appendChild(device_id);

                    console.log("device name is:"+deviceData.Device.name);

                    const start = document.createElement('td');
                    start.textContent = formatDate(device.start);
                    row.appendChild(start);

                    const end = document.createElement('td');
                    end.textContent = formatDate(device.end);
                    row.appendChild(end);

                    const orientation = document.createElement('td');
                    orientation.textContent = device.orientation;
                    row.appendChild(orientation);


                    // Create the table cell for the status
                    const statusCell = document.createElement('td');
                    // For each schedule, update the status depending on the date: waiting | showing now | completed
                    start_sch = new Date(device.start);
                    end_sch = new Date(device.end);
                    let statusText = "";  // Use a different name for the status text variable

                    if (start_sch > Date.now()) {
                        statusText = "waiting";
                        statusCell.style.color = "orange";
                    } else if (start_sch < Date.now() && end_sch > Date.now()) {
                        statusText = "Playing now";
                        statusCell.style.color = "green";
                    } else if (Date.now() > end_sch) {
                        statusText = "Completed";
                        statusCell.style.color = "red";
                    }
                    statusCell.textContent = statusText;
                    row.appendChild(statusCell);


                    const tools = document.createElement('td');
                    const editButton = document.createElement('button');
                    editButton.textContent = 'Edit';
                    editButton.onclick = () => editScheduleToggleForm(device); // Edit functionality
                    editButton.classList.add('btn', 'btn-primary');
                    editButton.style.marginRight = '5px';

                    const deleteButton = document.createElement('button');
                    deleteButton.textContent = 'Delete';
                    deleteButton.onclick = () => deleteSchedule(device.id); // Delete functionality
                    deleteButton.classList.add('btn', 'btn-danger');

                    tools.appendChild(editButton);
                    tools.appendChild(deleteButton);
                    row.appendChild(tools);

                    tableBody.appendChild(row);

                    count_fetched++
                } catch (e) {
                    console.error('Error fetching additional data:', e.message);

                    
                    // Delete the schedule if the ad or device is missing
                    if (e.message.includes('undefined')) {
                        await fetch(`url+'/schedules/${device.id}`, { method: 'DELETE' });
                        console.log(`Deleted schedule with ID: ${device.id}`);
                    }
                }
            }
        }
    } catch (error) {
        console.error('Error fetching schedules:', error);
    }finally{
        if(count_fetched===0){
            // If no devices, show the "Empty" row
            const emptyRow = document.createElement('tr');
            const emptyCell = document.createElement('td');
            emptyCell.colSpan = 7; // Span across all columns
            emptyCell.style.textAlign = 'center';
            emptyCell.style.verticalAlign = 'middle';
            emptyCell.textContent = 'Empty';
            emptyRow.appendChild(emptyCell);
            tableBody.appendChild(emptyRow);
        }
    }

    
    
}
*/
async function fetchSchedules() {
    const tableBody = document.querySelector('#scheduleTable tbody');
    let count_fetched = 0;
    try {
        // Fetch schedules, devices, and ads
        const [scheduleResponse, deviceResponse, adResponse] = await Promise.all([
            fetch(url+'/schedules'),
            fetch(url+'/devices'),
            fetch(url+'/ads'),
        ]);

        const schedules = await scheduleResponse.json();
        const devices = await deviceResponse.json();
        const ads = await adResponse.json();

        // Create lookup maps for devices and ads
        const devicesMap = Object.fromEntries(devices.map(device => [device.id, device]));
        const adsMap = Object.fromEntries(ads.map(ad => [ad.id, ad]));

        // Clear existing rows (if any)
        tableBody.innerHTML = '';
        
        // Check if there are any devices
        if (schedules.length > 0) {
            // Iterate over the devices and create table rows
            for (const schedule of schedules) {
                const row = document.createElement('tr');
                try {
                    // Extract ad_title and device_name from the lookup maps
                    const ad = adsMap[schedule.ad_id];
                    const device = devicesMap[schedule.device_id];

                    console.log(ad)
                    
                    // Populate the table row
                    const ad_id = document.createElement('td');
                    ad_id.textContent = ad.title;
                    row.appendChild(ad_id);
                

                    const device_id = document.createElement('td');
                    device_id.textContent = device.name;
                    row.appendChild(device_id);


                    const start = document.createElement('td');
                    start.textContent = formatDate(schedule.start);
                    row.appendChild(start);

                    const end = document.createElement('td');
                    end.textContent = formatDate(schedule.end);
                    row.appendChild(end);

                    const orientation = document.createElement('td');
                    orientation.textContent = schedule.orientation;
                    row.appendChild(orientation);


                    // Create the table cell for the status
                    const statusCell = document.createElement('td');
                    // For each schedule, update the status depending on the date: waiting | showing now | completed
                    start_sch = new Date(formatDate(schedule.start));
                    end_sch = new Date(formatDate(schedule.end));
                    let statusText = "";  // Use a different name for the status text variable

                    if (start_sch > Date.now()) {
                        statusText = "waiting";
                        statusCell.style.color = "orange";
                    } else if (start_sch < Date.now() && end_sch > Date.now()) {
                        statusText = "Playing now";
                        statusCell.style.color = "green";
                    } else if (Date.now() > end_sch) {
                        statusText = "Completed";
                        statusCell.style.color = "red";
                    }
                    statusCell.textContent = statusText;
                    row.appendChild(statusCell);


                    const tools = document.createElement('td');
                    //i removed edit button , return it if needed due to its multiple bugs
                    const editButton = document.createElement('button');
                    editButton.textContent = 'Edit';
                    editButton.onclick = () => editScheduleToggleForm(schedule); // Edit functionality
                    editButton.classList.add('btn', 'btn-primary');
                    editButton.style.marginRight = '5px';
                    editButton.style.display = 'none';  // Hides the button

                

                    const deleteButton = document.createElement('button');
                    deleteButton.textContent = 'Delete';
                    deleteButton.onclick = () => deleteSchedule(schedule.id); // Delete functionality
                    deleteButton.classList.add('btn', 'btn-danger');

                    tools.appendChild(editButton);
                    tools.appendChild(deleteButton);
                    row.appendChild(tools);

                    tableBody.appendChild(row);

                    count_fetched++
                } catch (e) {
                    console.error('Error fetching additional data:', e.message);

                    
                    // Delete the schedule if the ad or device is missing
                    if (e.message.includes('undefined')) {
                        await fetch(`url+'/schedules/${schedule.id}`, { method: 'DELETE' });
                        console.log(`Deleted schedule with ID: ${schedule.id}`);
                    }
                }
            }
        }
    } catch (error) {
        console.error('Error fetching schedules:', error);
    }finally{
        if(count_fetched===0){
            // If no devices, show the "Empty" row
            const emptyRow = document.createElement('tr');
            const emptyCell = document.createElement('td');
            emptyCell.colSpan = 7; // Span across all columns
            emptyCell.style.textAlign = 'center';
            emptyCell.style.verticalAlign = 'middle';
            emptyCell.textContent = 'Empty';
            emptyRow.appendChild(emptyCell);
            tableBody.appendChild(emptyRow);
        }
    }

    
    
}


//delete functions
async function deleteDevice(deviceId) {
    try {
        const response = await fetch(url+'/devices/' + deviceId, {
            method: 'DELETE',
        });
        
        if (response.ok) {
            //refrech:
            showSection('devices')
            alert('Device deleted successfully!');
        } else {
            alert('Failed to delete device.');
        }
    } catch (error) {
        console.error('Error deleting device:', error);
        alert('Error deleting device.');
    }
}
async function deleteAd(id) {
    try {
        const response = await fetch(url+'/ads/' + id, {
            method: 'DELETE',
        });
        
        if (response.ok) {
            //refrech:
            showSection('ads')
            alert('Ad deleted successfully!');
            
        } else {
            alert('Failed to delete Ad.');
        }
    } catch (error) {
        console.error('Error deleting Ad:', error);
        alert('Error deleting Ad.');
    }
}
async function deleteSchedule(id) {
    try {
        const response = await fetch(url+'/schedules/' + id, {
            method: 'DELETE',
        });
        
        if (response.ok) {
            //refrech:
            showSection('schedule')
            alert('schedule deleted successfully!');
        } else {
            alert('Failed to delete schedule.');
        }
    } catch (error) {
        console.error('Error deleting schedule:', error);
        alert('Error deleting schedule.');
    }
}



//add function
async function addDevice(event) {
    event.preventDefault(); // Prevent form default submission behavior

    // Extract data from the form
    const code = document.getElementById('deviceCodeInput').value;
    const name = document.getElementById('deviceNameInput').value;

    // Validation (optional)
    if (!code || !name) {
        alert('Please fill in all fields');
        return;
    }

    // Create the device object
    const newDevice = {
        id:code,
        name:name
    };

    // Send to server (optional, adjust API endpoint accordingly)
    try {
        const response = await fetch(url+'/devices/', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newDevice)
        });

        if (response.ok) {
            alert('added successfully!');
        } else {
            // Get the error message from the response body
            const errorData = await response.json();  // Assuming the response is JSON
            alert(errorData.message);  // Print the error message
        }
    } catch (error) {
        console.error('Error adding device:', error);
        alert('An error occurred.');
    } finally{
        showSection('devices')
    }
}
async function addAd(event) {
    event.preventDefault(); // Prevent form default submission behavior

    // Extract data from the form
    const title = document.getElementById('adTitleInput').value;
    const description = document.getElementById('adDescriptionInput').value;
    const type = document.getElementById('adTypeSelect').value;
    const media = document.getElementById('adMediaInput').files[0]; // Get the file selected by the user
    const fileExtension = '.'+media.name.split('.').pop(); // Get the file extension (e.g., 'jpg')


    // Validation (optional)
    if (!title || !description || !type || !media) {
        alert('Please fill in all fields');
        return;
    }

    // Create the ad object
    const adData = {
        title: title,
        description: description,
        type: type,
        mediaUrl:"loading",
        mediaExtension:fileExtension 
    };

    console.error(adData);

    // save the ad in server
    try {
        const response = await fetch(url+'/ads', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(adData)
        });

        if (response.ok) {
            alert('ad added successfully!');
            const data = await response.json();
            uploadAdMedia(data.ad.id,media)
            // Optional: Clear the form or reload the data
        } else {
            alert('Failed to add ad.');
        }
    } catch (error) {
        console.error('Error adding ad:', error);
        alert('An error occurred.');
    } finally{
        showSection('ads')
    }
}
async function addSchedule(event) {
    event.preventDefault(); // Prevent form default submission behavior

    // Extract data from the form
    const adId = document.getElementById('scheduleAdSelect').value;
    const deviceId = document.getElementById('scheduleDeviceSelect').value;
    const startTime = document.getElementById('scheduleStartInput').value;
    const endTime = document.getElementById('scheduleEndInput').value;

    // Validation (optional)
    if (!adId || !deviceId || !startTime || !endTime) {
        alert('Please fill in all fields');
        return;
    }

    if(isDateAfter(startTime ,endTime)){
        alert('end time must be after start time');
        return;
    }

    // Create the schedule object
    const newSchedule = {
        ad_id: adId,
        device_id: deviceId,
        start: startTime,
        end: endTime
    };

    console.log('Schedule data:', newSchedule);

    // Send to server (optional, adjust API endpoint accordingly)
    try {
        const response = await fetch(url+'/schedules', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newSchedule)
        });

        if (response.ok) {
            alert('Schedule added successfully!');
            // Optional: Clear the form or reload the data
        } else {
            // Parse the error response body
            const errorData = await response.json();
            alert('Failed to add schedule.\n' + errorData.message);
        }
    } catch (error) {
        console.error('Error adding schedule:', error);
        alert('An error occurred.');
    } finally{
        showSection('schedule')
    }
}
async function uploadAdMedia(id, media) {
    // Validation
    if (!id || !media) {
        alert('Please provide both Ad ID and media file.');
        return;
    }

    // Create FormData to handle file upload
    const formData = new FormData();
    formData.append('file', media); // 'file' matches the field in `upload.single()`

    try {
        // Send POST request to upload the file
        const response = await fetch(url+'/media/'+id, { // Adjusted URL to match the new route
            method: 'POST',
            body: formData, // Send FormData directly
        });

        if (response.ok) {
            const result = await response.json();
            alert(`Media uploaded successfully! Media ID: ${result.mediaId}`);
        } else {
            const error = await response.json();
            alert(`Failed to upload media: ${error.message}`);
        }
    } catch (error) {
        console.error('Error uploading media:', error);
        alert('An error occurred during upload.');
    }
}





//edit  functions
async function editDevice(id) {
    try {
        //get name from input
        var name  = document.getElementById('deviceNameInputEdit').value;

        if (!name) {
            alert('Please fill in all fields');
            return;
        }

        const updatedInput = {
            name: name
        };

        const response = await fetch(url+'/devices/' + id, {
            method: 'PUT',  // Use PUT for updating a resource
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updatedInput),  // Send the updated device data
        });

        if (response.ok) {
            // Refresh the device list after update
            showSection('devices');
            alert('Device updated successfully!');
        } else {
            alert('Failed to update device.');
        }
    } catch (error) {
        console.error('Error while updating device:', error);
        alert('Error while updating device');
    }
}
async function editAd(id) {
    event.preventDefault(); // Prevent form default submission behavior

    // Extract data from the form
    const title = document.getElementById('adTitleInputEdit').value;
    const description = document.getElementById('adDescriptionInputEdit').value;
    const type = document.getElementById('adTypeSelectEdit').value;
    const media = document.getElementById('adMediaInputEdit').files[0]; // Get the file selected by the user
    const fileExtension = '.'+media.name.split('.').pop(); // Get the file extension (e.g., 'jpg')


    // Validation (optional)
    if (!title || !description || !type || !media) {
        alert('Please fill in all fields');
        return;
    }

    // Create the ad object
    const adData = !media ? {
        title: title,
        description: description,
        type: type,
    } : {
        title: title,
        description: description,
        type: type,
        mediaUrl:"loading",
        mediaExtension:fileExtension 
    };

    console.error(adData);

    // Send to server (optional, adjust API endpoint accordingly)
    try {
        const response = await fetch(url+'/ads/'+id, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(adData)
        });

        if (response.ok) {
            alert('ad added successfully!');

            if(media){
                const data = await response.json();
                uploadAdMedia(data.ad.id,media)
            }
        } else {
            alert('Failed to update ad.');
        }
    } catch (error) {
        console.error('Error updating ad:', error);
        alert('An error occurred.');
    } finally{
        showSection('ads')
    }
}
async function editSchedule(id) {
    try {
        // Extract data from the form
        const adId = document.getElementById('scheduleAdSelectEdit').value;
        const deviceId = document.getElementById('scheduleDeviceSelectEdit').value;
        const startTime = document.getElementById('scheduleStartInputEdit').value;
        const endTime = document.getElementById('scheduleEndInputEdit').value;

        // Validation (optional)
        if (!adId || !deviceId || !startTime || !endTime) {
            alert('Please fill in all fields');
            return;
        }

        // Create the schedule object
        const updatedInput = {
            ad_id: adId,
            device_id: deviceId,
            start: startTime,
            end: endTime
        };


        const response = await fetch(url+'/schedules/' + id, {
            method: 'PUT',  // Use PUT for updating a resource
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updatedInput),  // Send the updated device data
        });

        if (response.ok) {
            alert('Schedule updated successfully!');
            // Refresh the device list after update
            showSection('schedule');
        } else {
            // Parse the error response body
            const errorData = await response.json();
            alert('Failed to update schedule.\n' + errorData.message);
            // Refresh the device list after update
            showSection('schedule');
        }
    } catch (error) {
        console.error('Error while updating device:', error);
        alert('Error while updating device');
        showSection('schedule');
    }
}



//toggle edit forms
async function editDeviceToggleForm(data) {
    try {
        const id = data.id;
        const name = data.name;

        // Set placeholders
        document.getElementById('deviceNameInputEdit').value = name;

        // Get the form element
        const form = document.getElementById('deviceFormEdit');
        
        if (id === selected_edit_device) {
            // If the selected device is the same as the one being toggled, hide the form
            form.style.display = 'none';
            selected_edit_device = ""; // Reset selected device
        } else {
            // Otherwise, show the form and set the selected device id
            form.style.display = 'block';
            selected_edit_device = id;

            // Add the event listener to the button
            const updateButton = document.getElementById('update_device_button');
            updateButton.addEventListener('click', function(event) {
                event.preventDefault();
                editDevice(id);
            });


            window.scrollTo({
                top: 0,
                behavior: 'smooth' // Optional: Makes the scroll smooth
            });
        }
    } catch (error) {
        console.error('Error updating', error);
        alert('Error while updating');
    }
}
async function editAdToggleForm(data) {
    try {
        const id = data.id;
        const title = data.title;
        const description = data.description;
        const type = data.type;
        const media = data.mediaUrl;


        // Set placeholders
        document.getElementById('adTitleInputEdit').value = title
        document.getElementById('adDescriptionInputEdit').value = description

        if(type==='image'){
            document.getElementById('image_option_edit').selected = true
        }else if(type==='music'){
            document.getElementById('music_option_edit').selected = true
        }else if(type==='video'){
            document.getElementById('video_option_edit').selected = true
        }

        // Get the form element
        const form = document.getElementById('adFormEdit');
        
        if (id === selected_edit_ad) {
            form.style.display = 'none';
            selected_edit_ad = ""; // Reset selected device
        } else {
            form.style.display = 'block';
            selected_edit_ad = id;

            // Add the event listener to the button
            const updateButton = document.getElementById('update_ad_button');
            updateButton.addEventListener('click', function(event) {
                event.preventDefault();
                editAd(id);
            });
            
            window.scrollTo({
                top: 0,
                behavior: 'smooth' // Optional: Makes the scroll smooth
            });
            
        }
    } catch (error) {
        console.error('Error updating', error);
        alert('Error while updating');
    }
}
async function editScheduleToggleForm(data) {
    try {
        const id = data.id;
        const device_id = data.device_id;
        const ad_id = data.ad_id;
        const start = new Date(data.start);
        const end = new Date(data.end);

        // Set placeholders
        add_ads_in_schedule_droplist('scheduleAdSelectEdit',ad_id)
        add_devices_in_schedule_droplist('scheduleDeviceSelectEdit',device_id)
        document.getElementById('scheduleStartInputEdit').value = start.toLocaleString("sv-SE").slice(0, 16);
        document.getElementById('scheduleEndInputEdit').value = end.toLocaleString("sv-SE").slice(0, 16);

        // Get the form element
        const form = document.getElementById('scheduleFormEdit');
        
        if (id === selected_edit_schedule) {
            form.style.display = 'none';
            selected_edit_schedule = ""; // Reset selected device
        } else {
            form.style.display = 'block';
            selected_edit_schedule = id;

            // Add the event listener to the button
            const updateButton = document.getElementById('update_schedule_button');
            updateButton.addEventListener('click', function(event) {
                event.preventDefault();
                editSchedule(id);
            });
            
        }
    } catch (error) {
        console.error('Error updating', error);
        alert('Error while updating');
    }
}


//logout -----------------------------------------------------------------------
async function logout() {
    try {
        const response = await fetch(url+'/logout', {
            method: 'POST',
            credentials: 'include', // Ensures cookies are sent with the request
        });

        if (response.redirected) {
            // Redirect the user to the login page
            window.location.href = response.url;
        } else {
            const message = await response.text();
            alert('Failed to log out: ' + message);
        }
    } catch (error) {
        console.error('Error during logout:', error);
        alert('An error occurred while logging out.');
    }
}



// others -------------------------------------------------------------------------------------------------------------
function showSection(section) {
    hideAllForms(); 
    document.querySelectorAll('.content section').forEach(sec => {
        sec.classList.add('inactive-section');
    });
    document.getElementById(section).classList.remove('inactive-section');
    document.getElementById(section).classList.add('active-section');


    selected_edit_device = ""
    selected_edit_ad = ""
    selected_edit_schedule = ""

    if(section=="devices"){
        fetchDevices()
    }else if(section=="ads"){
        fetchAds()
    }else if(section=="schedule"){
        add_ads_in_schedule_droplist()
        add_devices_in_schedule_droplist()
        fetchSchedules()
    }
}
function toggleForm(formId) {
    const form = document.getElementById(formId);
    form.style.display = form.style.display === 'block' ? 'none' : 'block';
}
function hideAllForms() {
    document.querySelectorAll('.form-container').forEach(form => {
        form.style.display = 'none';
    });
}
function formatDate(dateString) {
    // Check if the dateString is null or invalid
    if (dateString === null || isNaN(new Date(dateString))) {
        return 'N/A';
    }
    const date = new Date(dateString);

    // Get the year, month, day, hours, minutes, and AM/PM
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are 0-indexed, so we add 1
    const day = String(date.getDate()).padStart(2, '0');
    
    let hours = date.getHours();
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    hours = hours ? hours : 12; // Hour '0' should be '12'

    // Return the formatted date string
    return `${year}-${month}-${day} ${hours-1}:${minutes} ${ampm}`;
}
function isDateAfter(date1, date2) {
    // Convert the dates to Date objects if they are not already
    const d1 = new Date(date1);
    const d2 = new Date(date2);

    // Compare the dates
    return d1 > d2;
}
async function add_ads_in_schedule_droplist(list_id='scheduleAdSelect',selected_ad_id='') {
    try {
        // Fetch data from the server
        const response = await fetch(url+'/ads');
        const ads = await response.json();

        // Find the select element
        const adSelect = document.getElementById(list_id);

        // Clear any existing options
        adSelect.innerHTML = '';

        // Add a placeholder option
        const placeholderOption = document.createElement('option');
        placeholderOption.value = '';
        placeholderOption.textContent = 'Select an Ad';
        placeholderOption.disabled = true;
        placeholderOption.selected = true;
        adSelect.appendChild(placeholderOption);

        // Populate the dropdown with ads
        ads.forEach(ad => {
            const option = document.createElement('option');
            option.value = ad.id; // Assuming `id` is the unique identifier
            option.textContent = ad.title; // Displaying the ad title
            adSelect.appendChild(option);

            if (ad.id === selected_ad_id) {
                option.selected = true;
            }
        });
    } catch (error) {
        console.error('Error fetching ads:', error);
    }
}
async function add_devices_in_schedule_droplist(list_id='scheduleDeviceSelect',selected_device_id='') {
    try {
        // Fetch data from the server
        const response = await fetch(url+'/devices');
        const devices = await response.json();

        // Find the select element
        const deviceSelect = document.getElementById(list_id);

        // Clear any existing options
        deviceSelect.innerHTML = '';

        // Add a placeholder option
        const placeholderOption = document.createElement('option');
        placeholderOption.value = '';
        placeholderOption.textContent = 'Select a device';
        placeholderOption.disabled = true;
        placeholderOption.selected = true;
        deviceSelect.appendChild(placeholderOption);

        devices.forEach(device => {
            const option = document.createElement('option');
            option.value = device.id;
            option.textContent = device.name;
            deviceSelect.appendChild(option);

            if (device.id === selected_device_id) {
                option.selected = true;
            }
        });

    } catch (error) {
        console.error('Error fetching ads:', error);
    }
}
async function addAnotherAd() {
    const adContainer = document.getElementById("ads_of_schedule");

    // Create a new select element
    const newSelect = document.createElement("select");
    newSelect.classList.add("form-control");
    newSelect.innerHTML = '';
    newSelect.id = 'scheduleAdSelect'
    newSelect.style.marginTop = "5px";
    newSelect.style.marginBottom = "5px";

    await add_ads_in_schedule_droplist('','')

    // Insert the new select element before the "Add Another Ad" button
    const addButton = adContainer.querySelector("#the_ad_add_button_to_schedule");
    adContainer.insertBefore(newSelect, addButton);
}

//-----------------------------------------------------------------------------------------------------
// Show "Devices" section on load
let selected_edit_device = ""
let selected_edit_ad = ""
let selected_edit_schedule = ""
let isFetchingSchedules = false; // Flag to prevent overlapping calls: ma taamel fetch lel schedules ella ma tkamel el fetches lo5rin
showSection('devices');



let isUploading = false; // Flag to track upload state

