let currentPage = 0;
const pageSize = 10;
let totalPages = 0;
let activeFilters = {};
const API_URL = "/api/employees";

// Common API request method
async function apiRequest(url, method = "GET", body = null) {

    const userUUID = crypto.randomUUID();

    const headers = {
        "X-UUID": userUUID
    };

    console.log("Generated UUID:", userUUID);
    console.log("Request Headers:", headers);

    const options = {
        method: method,
        headers: headers
    };

    if (body !== null) {
        headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);

    console.log("Response Status:", response.status);
    console.log("Response X-UUID:", response.headers.get("X-UUID"));
    console.log("Response Request ID:", response.headers.get("X-Request-Id"));

    if (!response.ok) {
        const errorBody = await response.json().catch(() => null);
        throw new Error(
            errorBody?.message || `Request failed: ${response.status}`
        );
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}


// Load all employees

async function loadEmployees(page = currentPage) {

    const tableBody =
        document.getElementById("employeeTableBody");

    tableBody.innerHTML = `
        <tr>
            <td colspan="6" class="empty-message">
                Loading employees...
            </td>
        </tr>
    `;

    try {

let apiUrl;

if (Object.keys(activeFilters).length > 0) {

    // Call filter API when filters are selected
    const params = new URLSearchParams({
        ...activeFilters,
        page: page,
        size: pageSize,
        sortBy: "name",
        direction: "asc"
    });

    apiUrl = `${API_URL}/filter?${params.toString()}`;

} else {

    // Call pagination API when no filters are selected
    apiUrl = `${API_URL}/pagination?page=${page}&size=${pageSize}`;
}

const response = await apiRequest(apiUrl);

        currentPage = response.number;
        totalPages = response.totalPages;

        const employees = response.content;

        tableBody.innerHTML = "";

        if (employees.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-message">
                        No employees found.
                    </td>
                </tr>
            `;
        }

        employees.forEach(employee => {

            const row = document.createElement("tr");

            const values = [
                employee.id,
                employee.name,
                employee.email,
                employee.department,
                employee.salary
            ];

            values.forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value ?? "";
                row.appendChild(cell);
            });

            const actionsCell = document.createElement("td");

            const editButton = document.createElement("button");
            editButton.textContent = "Edit";
            editButton.className = "edit-btn";
            editButton.addEventListener("click", () => {
                editEmployee(employee.id);
            });

            const deleteButton = document.createElement("button");
            deleteButton.textContent = "Delete";
            deleteButton.className = "delete-btn";
            deleteButton.addEventListener("click", () => {
                deleteEmployee(employee.id);
            });

            actionsCell.append(editButton, deleteButton);
            row.appendChild(actionsCell);

            tableBody.appendChild(row);
        });

        updatePagination();

    } catch (error) {

        showMessage(error.message, "red");
    }
}



// Open Add Employee modal
function openAddModal() {

    document.getElementById("employeeForm").reset();

    document.getElementById("employeeId").value = "";

    document.getElementById("modalTitle").textContent =
        "Add Employee";

    document.getElementById("employeeModal").style.display =
        "flex";
}


// Open Edit Employee modal
async function editEmployee(id) {

    try {

        const employee = await apiRequest(`${API_URL}/${id}`);

        document.getElementById("employeeId").value =
            employee.id;

        document.getElementById("name").value =
            employee.name;

        document.getElementById("email").value =
            employee.email;

        document.getElementById("department").value =
            employee.department;

        document.getElementById("salary").value =
            employee.salary;

        document.getElementById("modalTitle").textContent =
            "Edit Employee";

        document.getElementById("employeeModal").style.display =
            "flex";

    } catch (error) {
        showMessage(error.message, "red");
    }
}


// Add or Update Employee
document.getElementById("employeeForm")
    .addEventListener("submit", async function(event) {

        event.preventDefault();

        const id = document.getElementById("employeeId").value;

        const employee = {
            name: document.getElementById("name").value.trim(),
            email: document.getElementById("email").value.trim(),
            department: document.getElementById("department").value.trim(),
            salary: Number(document.getElementById("salary").value)
        };

        try {

            if (id) {

                await apiRequest(
                    `${API_URL}/${id}`,
                    "PUT",
                    employee
                );

                showMessage("Employee updated successfully!", "green");

            } else {

                await apiRequest(
                    API_URL,
                    "POST",
                    employee
                );

                showMessage("Employee added successfully!", "green");
            }

            closeModal();
            await loadEmployees();

        } catch (error) {
            showMessage(error.message, "red");
        }
    });


// Delete Employee
async function deleteEmployee(id) {

    const confirmed = confirm(
        "Are you sure you want to delete this employee?"
    );

    if (!confirmed) {
        return;
    }

    try {

        await apiRequest(`${API_URL}/${id}`, "DELETE");

        showMessage("Employee deleted successfully!", "green");

        await loadEmployees();

    } catch (error) {
        showMessage(error.message, "red");
    }
}


// Close modal
function closeModal() {
    document.getElementById("employeeModal").style.display =
        "none";
}


// Display message
function showMessage(message, color) {

    const messageElement = document.getElementById("message");

    messageElement.textContent = message;
    messageElement.style.color = color;

}

function previousPage() {

    if (currentPage > 0) {
        loadEmployees(currentPage - 1);
    }
}

function nextPage() {

    if (currentPage < totalPages - 1) {
        loadEmployees(currentPage + 1);
    }
}

function updatePagination() {

    document.getElementById("pageInfo").textContent =
        `Page ${totalPages === 0 ? 0 : currentPage + 1} of ${totalPages}`;

    document.getElementById("prevBtn").disabled =
        currentPage === 0;

    document.getElementById("nextBtn").disabled =
        currentPage >= totalPages - 1;
}

function applyFilters() {

    const name = document.getElementById("filterName").value.trim();
    const department = document.getElementById("filterDepartment").value.trim();
    const minSalary = document.getElementById("minSalary").value;
    const maxSalary = document.getElementById("maxSalary").value;

    if (minSalary !== "" && maxSalary === "" ||
        minSalary === "" && maxSalary !== "") {

        showMessage("Please enter both minimum and maximum salary.", "red");
        return;
    }
    if (minSalary !== "" && maxSalary !== "") {
        if (Number(minSalary) > Number(maxSalary)) {
            showMessage(
                "Minimum salary cannot be greater than maximum salary.",
                "red"
            );
            return;
        }
    }

    activeFilters = {};

    if (name) activeFilters.name = name;
    if (department) activeFilters.department = department;

    if (minSalary !== "" && maxSalary !== "") {
        activeFilters.minSalary = minSalary;
        activeFilters.maxSalary = maxSalary;
    }

    if (Object.keys(activeFilters).length === 0) {
        showMessage(
            "Please provide at least one search filter: name, department, minSalary or maxSalary.",
            "red"
        );
        return;
    }

    loadEmployees(0);
}

function resetFilters() {
    document.getElementById("filterName").value = "";
    document.getElementById("filterDepartment").value = "";
    document.getElementById("minSalary").value = "";
    document.getElementById("maxSalary").value = "";

    activeFilters = {};

    // Clear previous error or success message
    document.getElementById("message").textContent = "";

    loadEmployees(0);
}

async function loadAllEmployees(page = 0) {

    const tableBody =
        document.getElementById("employeeTableBody");

    try {

        const response = await apiRequest(
            `${API_URL}/pagination?page=${page}&size=${pageSize}`
        );

        currentPage = response.number;
        totalPages = response.totalPages;

        // Reuse the existing table-rendering logic here.
        // Move the employee table rendering code into a
        // separate renderEmployees(response.content) function.

    } catch (error) {
        showMessage(error.message, "red");
    }
}


// Load employees when page opens
loadEmployees();