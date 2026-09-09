package com.example.debit.ui.utils

import com.example.debit.data.AppLanguage

object AppStrings {
    fun getCategoryName(category: String, language: AppLanguage): String {
        if (language == AppLanguage.ZH) return category
        return when (category) {
            "餐飲" -> "Dining"
            "日常" -> "Daily"
            "娛樂" -> "Entertainment"
            "購物" -> "Shopping"
            "交通" -> "Transport"
            "醫療" -> "Medical"
            "居住" -> "Housing"
            "其他" -> "Other"
            else -> category
        }
    }

    fun getSubCategoryName(sub: String, language: AppLanguage): String {
        if (language == AppLanguage.ZH) return sub
        return when (sub) {
            "早餐" -> "Breakfast"
            "午餐" -> "Lunch"
            "晚餐" -> "Dinner"
            "宵夜" -> "Late Snack"
            "點心飲料" -> "Snacks & Drinks"
            "洗衣" -> "Laundry"
            "烘衣" -> "Dryer"
            "電費" -> "Electricity"
            "日用品" -> "Household"
            else -> sub
        }
    }

    fun get(key: String, language: AppLanguage): String {
        val isZh = language == AppLanguage.ZH
        return when (key) {
            "settings" -> if (isZh) "偏好設定與工具" else "Settings & Tools"
            "set_budget" -> if (isZh) "規劃月度小金庫" else "Plan Monthly Budget"
            "monthly_budget" -> if (isZh) "本月目標金庫 ($)" else "Total Monthly Target ($)"
            "theme_color" -> if (isZh) "色彩主題" else "Theme Color"
            "app_language" -> if (isZh) "語言偏好" else "Language"
            "add_record" -> if (isZh) "記一筆" else "Add Record"
            "add_expense" -> if (isZh) "新增一筆開銷" else "Add Expense"
            "edit_record" -> if (isZh) "修改記帳內容" else "Edit Record"
            "expense_amount" -> if (isZh) "消費金額 ($)" else "Expense Amount ($)"
            "select_date" -> if (isZh) "日期" else "Date"
            "change_date" -> if (isZh) "換個日期" else "Change Date"
            "select_category" -> if (isZh) "這是什麼樣的消費呢？" else "Category"
            "note_optional" -> if (isZh) "寫點小備註吧 (選填)" else "Note (Optional)"
            "laundry_chip" -> if (isZh) "洗衣 ($20)" else "Laundry ($20)"
            "dryer_chip" -> if (isZh) "烘衣 ($20)" else "Dryer ($20)"
            "monthly_overview" -> if (isZh) "本月金庫進度" else "Monthly Progress"
            "monthly_status" -> if (isZh) "本月金庫概況" else "Monthly Budget Status"
            "daily_status" -> if (isZh) "今日額度概況" else "Today's Budget Status"
            "spent" -> if (isZh) "已花費" else "Spent"
            "month_spent" -> if (isZh) "本月已花費" else "Month Spent"
            "today_spent" -> if (isZh) "今天已花費" else "Today Spent"
            "remaining" -> if (isZh) "金庫還剩" else "Remaining"
            "month_remaining" -> if (isZh) "本月剩餘金庫" else "Month Remaining"
            "today_remaining" -> if (isZh) "今天還能花" else "Today Remaining"
            "over_budget" -> if (isZh) "稍微超支囉" else "Over Budget"
            "month_over" -> if (isZh) "本月超支" else "Month Over"
            "today_over" -> if (isZh) "今天超支" else "Today Over"
            "near_limit" -> if (isZh) "快花到上限囉" else "Near Limit"
            "today_ample" -> if (isZh) "今天額度很充裕" else "On Track"
            "month_progress" -> if (isZh) "金庫用量" else "Month Progress"
            "total_budget" -> if (isZh) "月預算目標" else "Total Budget"
            "avg_daily_budget" -> if (isZh) "每日平均額度" else "Daily Budget"
            "dynamic_daily_budget" -> if (isZh) "動態靈活配額" else "Flex Allowance"
            "records_breakdown" -> if (isZh) "記帳細項紀錄" else "Itemized Records"
            "subtotal" -> if (isZh) "當天小計" else "Subtotal"
            "no_records" -> if (isZh) "今天還沒記帳呢！要來紀錄第一筆小開銷嗎？" else "No records yet! Add your first record "
            "add_first" -> if (isZh) "開始第一筆記帳" else "Add First Record"
            "pie_chart" -> if (isZh) "圓餅圖分佈" else "Category Pie Chart"
            "daily_chart" -> if (isZh) "每日消費趨勢" else "Daily Usage Chart"
            "budget_usage" -> if (isZh) "金庫已使用" else "Budget Usage"
            "total_expense" -> if (isZh) "總花費" else "Total Expense"
            "unused_budget" -> if (isZh) "尚未花費" else "Unused Budget"
            "daily_avg" -> if (isZh) "平均每日" else "Daily Avg"
            "daily_max" -> if (isZh) "單日最高" else "Daily Max"
            "day_unit" -> if (isZh) "日" else "Day "
            "save" -> if (isZh) "儲存紀錄" else "Save"
            "update_record" -> if (isZh) "儲存修改" else "Update Record"
            "cancel" -> if (isZh) "取消" else "Cancel"
            "close" -> if (isZh) "關閉" else "Close"
            "confirm" -> if (isZh) "確定" else "OK"
            "delete_item" -> if (isZh) "刪除這筆紀錄" else "Delete Record"
            "edit_item" -> if (isZh) "修改這筆紀錄" else "Edit Record"
            "select_report_month" -> if (isZh) "查看歷史月份 " else "Select Monthly Report"
            "month_report_hint" -> if (isZh) "點擊月份即可回顧該月份的支出細項與財務圖表分析。" else "Select a month to view detailed expenses, budget, and charts."
            "report_summary" -> if (isZh) "歷史報表概覽" else "Report Overview"
            "return_current_month" -> if (isZh) "回到這個月" else "Return to Current Month"
            "viewing_history" -> if (isZh) "正在回顧歷史資料" else "Viewing Historical Report"
            "welcome_title" -> if (isZh) "歡迎來到 Budget Manager " else "Welcome to Budget Manager"
            "welcome_subtitle" -> if (isZh) "輕鬆幾步設定，展開您的專屬智慧理財" else "Set up in a few simple steps to start smart budgeting"
            "start_app" -> if (isZh) "開始理財 " else "Get Started"
            "invalid_budget_hint" -> if (isZh) "哎呀！金額好像忘記填或填錯囉 " else "Please enter a valid budget amount"
            "daily_breakdown_title" -> if (isZh) "每日詳細花費明細" else "Daily Breakdown"
            "active_days" -> if (isZh) "記帳天數" else "Active Days"
            "days_unit" -> if (isZh) "天" else "Days"
            "click_to_view_detail" -> if (isZh) "點擊檢視詳細花費明細 ›" else "Tap for Daily Breakdown ›"
            else -> key
        }
    }
}
