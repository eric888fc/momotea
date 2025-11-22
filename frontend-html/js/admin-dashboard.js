"use strict";

// ===== 小工具 =====
const $ = id => document.getElementById(id);
const apiBase = "http://localhost:8080";

// (修正) 這裡要改成跟 login.js 一樣的 Key ('token' 和 'role')
const token = localStorage.getItem("token");      // 原本是 "authToken"
const role = localStorage.getItem("role");        // 原本是 "userRole"
const adminCode = localStorage.getItem("adminCode"); // 這個沒變

// (驗證)
if (!token || role !== "ADMIN") {
    alert("請先以系統管理員身分登入");
    location.href = "login.html";
}
$("adminCodeDisplay").textContent = adminCode || "-";

$("logoutBtn").onclick = () => {
    // (修正) 登出時也要清除正確的 Key
    ["token", "role", "userName", "adminCode"].forEach(k => localStorage.removeItem(k));
    location.href = "login.html";
};

async function apiGetJson(path) {
    const res = await fetch(apiBase + path, {
        headers: { "Authorization": "Bearer " + token }
    });
    if (!res.ok) throw new Error(await res.text() || "Request failed");
    return res.json();
}

async function apiDelete(path) {
    const res = await fetch(apiBase + path, {
        method: "DELETE",
        headers: { "Authorization": "Bearer " + token }
    });
    if (!res.ok) throw new Error(await res.text() || "Delete failed");
}

// ===== 報表圖表初始化 =====
const barRange = new Chart($("barChartRange").getContext("2d"), {
    type: "bar",
    data: {
        labels: ["小額(≤500)", "中額(500~2000)", "大額(≥2000)"],
        datasets: [{ data: [0, 0, 0] }]
    },
    options: {
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
    }
});

const pieMembers = new Chart($("pieChartMembers").getContext("2d"), {
    type: "pie",
    data: {
        labels: ["新增買家數", "新增賣家數"],
        datasets: [{ data: [0, 0] }]
    },
    options: { plugins: { legend: { position: "bottom" } } }
});

const barCategory = new Chart($("barChartCategory").getContext("2d"), {
    type: "bar",
    data: { labels: [], datasets: [{ data: [] }] },
    options: {
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
    }
});

const barFinance = new Chart($("barChartFinance").getContext("2d"), {
    type: "bar",
    data: { labels: ["本期營收", "上期營收"], datasets: [{ data: [0, 0] }] },
    options: {
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true } }
    }
});

// ===== 報表 UI =====
let currentPeriod = "weekly";

function fillText(report, label) {
    $("reportTitle").textContent = label + "營運報表";
    $("reportRange").textContent = `${report.startDate} ~ ${report.endDate}`;

    $("totalOrderCount").textContent = report.totalOrderCount;
    $("totalOrderAmount").textContent = report.totalOrderAmount;
    $("averageOrderAmount").textContent = report.averageOrderAmount;
    $("smallOrderCount").textContent = report.smallOrderCount;
    $("mediumOrderCount").textContent = report.mediumOrderCount;
    $("largeOrderCount").textContent = report.largeOrderCount;
    $("newBuyerCount").textContent = report.newBuyerCount;
    $("newSellerCount").textContent = report.newSellerCount;
    $("totalNewUserCount").textContent = report.totalNewUserCount;

    barRange.data.datasets[0].data = [
        report.smallOrderCount || 0,
        report.mediumOrderCount || 0,
        report.largeOrderCount || 0
    ];
    barRange.update();

    pieMembers.data.datasets[0].data = [
        report.newBuyerCount || 0,
        report.newSellerCount || 0
    ];
    pieMembers.update();

    const tbody = $("categoryTableBody");
    tbody.innerHTML = "";
    const labels = [];
    const data = [];
    (report.topCategories || []).forEach((c, i) => {
        const tr = document.createElement("tr");
        tr.innerHTML = `<td>${i + 1}</td><td>${c.categoryName}</td><td>${c.totalQuantity}</td>`;
        tbody.appendChild(tr);
        labels.push(c.categoryName);
        data.push(c.totalQuantity);
    });
    barCategory.data.labels = labels;
    barCategory.data.datasets[0].data = data;
    barCategory.update();
}

function fillFinancial(fin) {
    $("financialRange").textContent =
        `本期：${fin.currentStartDate} ~ ${fin.currentEndDate}　` +
        `上一期：${fin.previousStartDate} ~ ${fin.previousEndDate}`;

    $("curRevenue").textContent = fin.currentRevenue;
    $("prevRevenue").textContent = fin.previousRevenue;
    $("revDiff").textContent = fin.revenueDiff;
    $("revGrowth").textContent = fin.revenueGrowthRate + " %";
    $("curOrderCount").textContent = fin.currentOrderCount;
    $("prevOrderCount").textContent = fin.previousOrderCount;
    $("orderDiff").textContent = fin.orderCountDiff;
    $("orderGrowth").textContent = fin.orderCountGrowthRate + " %";

    barFinance.data.datasets[0].data = [
        fin.currentRevenue || 0,
        fin.previousRevenue || 0
    ];
    barFinance.update();
}

async function loadReport(period) {
    $("errorMsg").textContent = "";
    const url = period === "weekly" ? "/api/admin/reports/weekly" : "/api/admin/reports/quarterly";
    const label = period === "weekly" ? "本週" : "本季";

    try {
        const data = await apiGetJson(url);
        fillText(data, label);
    } catch (e) {
        console.error(e);
        $("errorMsg").textContent = "營運報表產生失敗";
    }

    try {
        const fin = await apiGetJson(`/api/admin/reports/financial?period=${period}`);
        fillFinancial(fin);
    } catch (e) {
        console.error(e);
    }
}

$("weeklyBtn").onclick = () => {
    currentPeriod = "weekly";
    $("weeklyBtn").classList.add("active");
    $("quarterlyBtn").classList.remove("active");
    loadReport("weekly");
};

$("quarterlyBtn").onclick = () => {
    currentPeriod = "quarterly";
    $("quarterlyBtn").classList.add("active");
    $("weeklyBtn").classList.remove("active");
    loadReport("quarterly");
};

$("exportBtn").onclick = async () => {
    try {
        const res = await fetch(`${apiBase}/api/admin/reports/export?period=${currentPeriod}`, {
            headers: { "Authorization": "Bearer " + token }
        });
        if (!res.ok) throw new Error("匯出失敗");
        const blob = await res.blob();
        const a = document.createElement("a");
        a.href = URL.createObjectURL(blob);
        a.download = currentPeriod === "weekly" ? "週報表.xlsx" : "季報表.xlsx";
        document.body.appendChild(a);
        a.click();
        a.remove();
    } catch (e) {
        alert(e.message || "匯出失敗");
    }
};

// ===== 會員管理 =====
const buyerBody = $("buyerTableBody");
const sellerBody = $("sellerTableBody");

const clearDetail = () => {
    $("manageDetailTitle").textContent = "請先選擇一位會員查看訂單 / 商品。";
    $("manageDetailHead").innerHTML = "";
    $("manageDetailBody").innerHTML = "";
};

const renderOrders = (list, title) => {
    $("manageDetailTitle").textContent = title;
    $("manageDetailHead").innerHTML =
        `<tr><th>訂單編號</th><th>買家</th><th>賣家</th><th>總金額</th><th>狀態</th><th>建立時間</th></tr>`;
    const body = $("manageDetailBody");
    body.innerHTML = "";
    list.forEach(o => {
        const tr = document.createElement("tr");
        tr.innerHTML = `<td>${o.orderId}</td><td>${o.buyerName || ""}</td><td>${o.sellerName || ""}</td>
                        <td>${o.totalPrice}</td><td>${o.status}</td><td>${o.createdAt}</td>`;
        body.appendChild(tr);
    });
};

const renderProducts = (list, title) => {
    $("manageDetailTitle").textContent = title;
    $("manageDetailHead").innerHTML =
        `<tr><th>商品編號</th><th>名稱</th><th>價格</th><th>庫存</th><th>狀態</th><th>建立時間</th></tr>`;
    const body = $("manageDetailBody");
    body.innerHTML = "";
    list.forEach(p => {
        const tr = document.createElement("tr");
        tr.innerHTML = `<td>${p.productId}</td><td>${p.name}</td><td>${p.price}</td>
                        <td>${p.stock}</td><td>${p.status}</td><td>${p.createdAt}</td>`;
        body.appendChild(tr);
    });
};

async function loadBuyers() {
    clearDetail();
    try {
        const list = await apiGetJson("/api/admin/users/buyers");
        buyerBody.innerHTML = "";
        list.forEach(u => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${u.userId}</td>
                <td>${u.name || ""}</td>
                <td>${u.email || ""}</td>
                <td>${u.phone || ""}</td>
                <td>${u.defaultAddress || ""}</td>
                <td>${u.createdAt || ""}</td>
                <td>
                    <button class="btn-table" data-type="buyer-orders" data-id="${u.userId}" data-name="${u.name}">查看訂單</button>
                    <button class="btn-table btn-danger" data-type="delete" data-id="${u.userId}" data-name="${u.name}">刪除帳號</button>
                </td>`;
            buyerBody.appendChild(tr);
        });
    } catch (e) {
        console.error(e);
        alert("讀取買家列表失敗");
    }
}

async function loadSellers() {
    clearDetail();
    try {
        const list = await apiGetJson("/api/admin/users/sellers");
        sellerBody.innerHTML = "";
        list.forEach(u => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${u.userId}</td>
                <td>${u.name || ""}</td>
                <td>${u.email || ""}</td>
                <td>${u.phone || ""}</td>
                <td>${u.defaultAddress || ""}</td>
                <td>${u.createdAt || ""}</td>
                <td>
                    <button class="btn-table" data-type="seller-orders" data-id="${u.userId}" data-name="${u.name}">查看訂單</button>
                    <button class="btn-table" data-type="seller-products" data-id="${u.userId}" data-name="${u.name}">查看上架商品</button>
                    <button class="btn-table btn-danger" data-type="delete" data-id="${u.userId}" data-name="${u.name}">刪除帳號</button>
                </td>`;
            sellerBody.appendChild(tr);
        });
    } catch (e) {
        console.error(e);
        alert("讀取賣家列表失敗");
    }
}

buyerBody.onclick = async e => {
    const btn = e.target.closest("button");
    if (!btn) return;
    const { type, id, name } = btn.dataset;

    if (type === "buyer-orders") {
        try {
            const list = await apiGetJson(`/api/admin/users/buyers/${id}/orders`);
            renderOrders(list, `買家「${name}」的訂單`);
        } catch (err) {
            console.error(err);
            alert("讀取訂單失敗");
        }
    } else if (type === "delete") {
        if (!confirm(`確定刪除買家「${name}」帳號？`)) return;
        try {
            await apiDelete(`/api/admin/users/${id}`);
            alert("刪除成功");
            loadBuyers();
        } catch (err) {
            console.error(err);
            alert("刪除失敗");
        }
    }
};

sellerBody.onclick = async e => {
    const btn = e.target.closest("button");
    if (!btn) return;
    const { type, id, name } = btn.dataset;

    if (type === "seller-orders") {
        try {
            const list = await apiGetJson(`/api/admin/users/sellers/${id}/orders`);
            renderOrders(list, `賣家「${name}」的訂單`);
        } catch (err) {
            console.error(err);
            alert("讀取訂單失敗");
        }
    } else if (type === "seller-products") {
        try {
            const list = await apiGetJson(`/api/admin/users/sellers/${id}/products`);
            renderProducts(list, `賣家「${name}」的上架商品`);
        } catch (err) {
            console.error(err);
            alert("讀取商品失敗");
        }
    } else if (type === "delete") {
        if (!confirm(`確定刪除賣家「${name}」帳號？`)) return;
        try {
            await apiDelete(`/api/admin/users/${id}`);
            alert("刪除成功");
            loadSellers();
        } catch (err) {
            console.error(err);
            alert("刪除失敗");
        }
    }
};

$("buyerManageBtn").onclick = () => {
    $("buyerManageBtn").classList.add("active");
    $("sellerManageBtn").classList.remove("active");
    $("buyerManageSection").style.display = "";
    $("sellerManageSection").style.display = "none";
    loadBuyers();
};
$("sellerManageBtn").onclick = () => {
    $("sellerManageBtn").classList.add("active");
    $("buyerManageBtn").classList.remove("active");
    $("buyerManageSection").style.display = "none";
    $("sellerManageSection").style.display = "";
    loadSellers();
};

// ===== 初始化 =====
$("weeklyBtn").click();      // 載入本週報表
$("buyerManageBtn").click(); // 預設顯示買家管理
