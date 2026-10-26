/* ==========================================================
   PAYROLL MANAGEMENT SYSTEM - MAIN JAVASCRIPT FILE
   ------------------------------------------------------------
   File  : src/main/resources/static/js/script.js
   Used by: all five HTML pages (loaded at the bottom of each page)

   What this file does:
     1. Opens and closes the sidebar on mobile screens
     2. Asks for confirmation before deleting a record
     3. Closes the green / red message boxes automatically
     4. Calculates the salary LIVE while the user types (preview)
     5. Fills employee details when a name is picked from the drop-down
     6. Small helpers: currency format, search box shortcuts, print page
   ========================================================== */

/* Runs after the whole page is loaded (DOMContentLoaded) */
document.addEventListener("DOMContentLoaded", function () {
    initSidebar();
    initDeleteConfirmation();
    initAutoHideAlerts();
    initSalaryPreview();
    initEmployeeAutoFill();
    initSearchShortcuts();
    initPrintButtons();
});

/* ==========================================================
   1. SIDEBAR (mobile friendly menu)
   ========================================================== */
function initSidebar() {
    var sidebar = document.getElementById("sidebar");
    var overlay = document.getElementById("overlay");
    var toggleButton = document.getElementById("sidebarToggle");

    if (!sidebar || !toggleButton) {
        return;
    }

    toggleButton.addEventListener("click", function () {
        sidebar.classList.toggle("open");
        if (overlay) {
            overlay.classList.toggle("show");
        }
    });

    // Clicking the dark area beside the menu closes it
    if (overlay) {
        overlay.addEventListener("click", function () {
            sidebar.classList.remove("open");
            overlay.classList.remove("show");
        });
    }

    // Close the menu when a link inside it is clicked
    var navLinks = sidebar.querySelectorAll(".nav-link");
    navLinks.forEach(function (link) {
        link.addEventListener("click", function () {
            sidebar.classList.remove("open");
            if (overlay) {
                overlay.classList.remove("show");
            }
        });
    });
}

/* ==========================================================
   2. DELETE CONFIRMATION
   Any element with data-confirm="..." shows a confirm box
   before its form is submitted. This protects against
   accidental deletion of an employee or a payroll record.
   ========================================================== */
function initDeleteConfirmation() {
    var items = document.querySelectorAll("[data-confirm]");

    items.forEach(function (item) {
        item.addEventListener("submit", function (event) {
            if (!window.confirm(item.getAttribute("data-confirm"))) {
                event.preventDefault();   // stop the form from being submitted
            }
        });

        // For links (if we ever use <a> instead of a form)
        item.addEventListener("click", function (event) {
            if (!window.confirm(item.getAttribute("data-confirm"))) {
                event.preventDefault();
            }
        });
    });
}

/* ==========================================================
   3. AUTO HIDE SUCCESS / ERROR MESSAGES
   ========================================================== */
function initAutoHideAlerts() {
    var alerts = document.querySelectorAll(".alert");

    alerts.forEach(function (alert) {
        var closeButton = alert.querySelector(".alert-close");
        if (closeButton) {
            closeButton.addEventListener("click", function () {
                alert.style.display = "none";
            });
        }

        // Error messages stay on the screen, success messages disappear after 6 seconds
        if (alert.classList.contains("alert-success")) {
            window.setTimeout(function () {
                alert.style.transition = "opacity 0.4s ease";
                alert.style.opacity = "0";
                window.setTimeout(function () {
                    alert.style.display = "none";
                }, 400);
            }, 6000);
        }
    });
}

/* ==========================================================
   4. LIVE SALARY PREVIEW
   The server (PayrollService) always does the real calculation.
   This JavaScript only shows an instant preview while typing,
   so the user sees the salary summary without pressing a button.
   ========================================================== */

/** Reads a number from an input box. Empty box = 0. */
function readNumber(id) {
    var element = document.getElementById(id);
    if (!element) {
        return 0;
    }
    var value = parseFloat(element.value);
    return isNaN(value) ? 0 : value;
}

/** Formats a number as Indian style currency, e.g. 55000 -> 55,000.00 */
function formatMoney(amount) {
    return Number(amount || 0).toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function initSalaryPreview() {
    var basicInput = document.getElementById("basicSalary");
    if (!basicInput) {
        return;   // we are not on the salary calculator page
    }

    var inputIds = ["basicSalary", "allowance", "bonus", "taxDeduction", "otherDeduction"];

    inputIds.forEach(function (id) {
        var element = document.getElementById(id);
        if (element) {
            element.addEventListener("input", updateSalaryPreview);
        }
    });

    updateSalaryPreview();
}

function updateSalaryPreview() {
    var basic = readNumber("basicSalary");
    var allowance = readNumber("allowance");
    var bonus = readNumber("bonus");
    var tax = readNumber("taxDeduction");
    var other = readNumber("otherDeduction");

    /* ---- the same formula used in PayrollService ---- */
    var grossSalary = basic + allowance + bonus;     // Gross Salary
    var totalDeduction = tax + other;                // Total Deduction
    var netSalary = grossSalary - totalDeduction;    // Net Salary

    setText("previewGross", formatMoney(grossSalary));
    setText("previewTotalDeduction", formatMoney(totalDeduction));
    setText("previewNet", formatMoney(netSalary));
    setText("previewBasic", formatMoney(basic));
    setText("previewAllowance", formatMoney(allowance));
    setText("previewBonus", formatMoney(bonus));

    /* ---- validation hint shown under the Net Salary ---- */
    var hint = document.getElementById("previewHint");
    if (hint) {
        if (netSalary < 0) {
            hint.textContent = "Warning: total deduction is greater than the gross salary.";
            hint.style.color = "#b91c1c";
        } else if (basic === 0 && allowance === 0 && bonus === 0) {
            hint.textContent = "Enter the salary details to see the live summary.";
            hint.style.color = "#64748b";
        } else {
            hint.textContent = "Live preview - press Calculate Salary to confirm on the server.";
            hint.style.color = "#047857";
        }
    }

    /* ---- highlight the summary card only when there is something to show ---- */
    var summaryCard = document.getElementById("livePreview");
    if (summaryCard) {
        summaryCard.style.display = (basic > 0 || allowance > 0 || bonus > 0) ? "block" : "none";
    }
}

function setText(id, text) {
    var element = document.getElementById(id);
    if (element) {
        element.textContent = text;
    }
}

/* ==========================================================
   5. EMPLOYEE AUTO FILL
   When a name is chosen in the drop-down, the Employee ID and
   the Basic Salary are filled in automatically. This shows how
   the HashMap lookup gives the employee record instantly.
   ========================================================== */
function initEmployeeAutoFill() {
    var select = document.getElementById("employeeSelect");
    if (!select) {
        return;
    }

    select.addEventListener("change", function () {
        var option = select.options[select.selectedIndex];

        if (!option || option.value === "") {
            return;
        }

        var employeeIdField = document.getElementById("employeeId");
        var basicSalaryField = document.getElementById("basicSalary");
        var infoBox = document.getElementById("selectedEmployeeInfo");

        if (employeeIdField) {
            employeeIdField.value = option.value;
        }
        if (basicSalaryField && option.getAttribute("data-basic")) {
            basicSalaryField.value = option.getAttribute("data-basic");

            // Suggest an HRA of 10% of the basic salary (only if the box is empty)
            var allowanceField = document.getElementById("allowance");
            if (allowanceField && allowanceField.value === "") {
                allowanceField.value = formatMoney(Number(option.getAttribute("data-basic")) * 0.1);
            }
        }

        if (infoBox) {
            infoBox.style.display = "block";
            setText("selName", option.getAttribute("data-name"));
            setText("employeeIdInfo", option.value);
            setText("selDept", option.getAttribute("data-department"));
            setText("selDesig", option.getAttribute("data-designation"));
            setText("selBasic", formatMoney(option.getAttribute("data-basic")));
        }

        updateSalaryPreview();
    });
}

/* ==========================================================
   6. SEARCH BOX SHORTCUTS
   Pressing "/" jumps to the search box, Escape clears it.
   ========================================================== */
function initSearchShortcuts() {
    var searchInput = document.getElementById("keyword");

    if (!searchInput) {
        return;
    }

    document.addEventListener("keydown", function (event) {
        var tagName = document.activeElement ? document.activeElement.tagName : "";

        if (event.key === "/" && tagName !== "INPUT" && tagName !== "TEXTAREA") {
            event.preventDefault();
            searchInput.focus();
        }

        if (event.key === "Escape" && document.activeElement === searchInput) {
            searchInput.value = "";
            searchInput.blur();
        }
    });
}

/* ==========================================================
   7. PRINT BUTTON  (used by the salary summary card)
   ========================================================== */
function initPrintButtons() {
    var buttons = document.querySelectorAll("[data-print]");

    buttons.forEach(function (button) {
        button.addEventListener("click", function () {
            window.print();
        });
    });
}

/* ==========================================================
   8. SMALL HELPER - used by the pages if needed:
      showMessage("Your text", "success")
   ========================================================== */
function showMessage(text, type) {
    var box = document.createElement("div");
    box.className = "alert alert-" + (type || "info");
    box.textContent = text;
    var body = document.querySelector(".page-body");
    if (body) {
        body.insertBefore(box, body.firstChild);
    }
}