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
            "日用品" -> "Household"
            else -> sub
        }
    }

    fun get(key: String, language: AppLanguage): String {
        val isZh = language == AppLanguage.ZH
        return when (key) {
            "settings" -> if (isZh) "設定選項" else "Settings"
            "set_budget" -> if (isZh) "設定月度預算" else "Set Monthly Budget"
            "monthly_budget" -> if (isZh) "全月總預算 ($)" else "Total Monthly Budget ($)"
            "theme_color" -> if (isZh) "主題色彩風格" else "Theme Accent Color"
            "app_language" -> if (isZh) "應用程式語言" else "Language"
            "add_record" -> if (isZh) "記一筆" else "Add Record"
            "add_expense" -> if (isZh) "新增支出" else "Add Expense"
            "edit_record" -> if (isZh) "修改記帳紀錄" else "Edit Record"
            "expense_amount" -> if (isZh) "支出金額 ($)" else "Expense Amount ($)"
            "select_date" -> if (isZh) "選擇日期" else "Select Date"
            "change_date" -> if (isZh) "更換日期" else "Change Date"
            "select_category" -> if (isZh) "選擇類別" else "Select Category"
            "meal_time" -> if (isZh) "餐飲時段 (可選)" else "Meal Time (Optional)"
            "daily_sub" -> if (isZh) "日常快捷細項 (點擊自動設定金額)" else "Household Quick Items (Tap for $20)"
            "note_optional" -> if (isZh) "備註 (可填)" else "Note (Optional)"
            "laundry_chip" -> if (isZh) "洗衣 ($20)" else "Laundry ($20)"
            "dryer_chip" -> if (isZh) "烘衣 ($20)" else "Dryer ($20)"
            "monthly_overview" -> if (isZh) "月度概覽" else "Monthly Overview"
            "monthly_status" -> if (isZh) "本月預算狀態" else "Monthly Budget Status"
            "daily_status" -> if (isZh) "今日預算狀態" else "Today's Budget Status"
            "spent" -> if (isZh) "已支出" else "Spent"
            "month_spent" -> if (isZh) "月已支出" else "Month Spent"
            "today_spent" -> if (isZh) "今日已支出" else "Today Spent"
            "remaining" -> if (isZh) "剩餘預算" else "Remaining"
            "month_remaining" -> if (isZh) "全月剩餘" else "Month Remaining"
            "today_remaining" -> if (isZh) "今日剩餘配額" else "Today Remaining"
            "over_budget" -> if (isZh) "已超支" else "Over Budget"
            "month_over" -> if (isZh) "月超額" else "Month Over"
            "today_over" -> if (isZh) "今日超額" else "Today Over"
            "near_limit" -> if (isZh) "接近上限" else "Near Limit"
            "today_ample" -> if (isZh) "今日預算充裕" else "On Track"
            "month_progress" -> if (isZh) "月進度" else "Month Progress"
            "total_budget" -> if (isZh) "全月總預算" else "Total Budget"
            "avg_daily_budget" -> if (isZh) "平均日配額" else "Daily Budget"
            "dynamic_daily_budget" -> if (isZh) "動態可支" else "Flex Allowance"
            "records_breakdown" -> if (isZh) "記帳紀錄分項" else "Itemized Records"
            "subtotal" -> if (isZh) "小計" else "Subtotal"
            "no_records" -> if (isZh) "本月尚無記帳紀錄" else "No records this month"
            "add_first" -> if (isZh) "新增第一筆記帳" else "Add First Record"
            "pie_chart" -> if (isZh) "分類圓餅圖" else "Category Pie Chart"
            "daily_chart" -> if (isZh) "每日用量圖" else "Daily Usage Chart"
            "budget_usage" -> if (isZh) "預算用量" else "Budget Usage"
            "total_expense" -> if (isZh) "總支出" else "Total Expense"
            "unused_budget" -> if (isZh) "未用預算" else "Unused Budget"
            "pie_chart_title" -> if (isZh) "預算佔比圓餅圖" else "Budget Pie Chart"
            "pie_chart_title_no_bgt" -> if (isZh) "本月支出分類統計" else "Category Expense Chart"
            "daily_avg" -> if (isZh) "平均每日" else "Daily Avg"
            "daily_max" -> if (isZh) "單日最高" else "Daily Max"
            "day_unit" -> if (isZh) "日" else "Day "
            "save" -> if (isZh) "儲存" else "Save"
            "update_record" -> if (isZh) "更新紀錄" else "Update Record"
            "cancel" -> if (isZh) "取消" else "Cancel"
            "close" -> if (isZh) "關閉" else "Close"
            "confirm" -> if (isZh) "確定" else "OK"
            "delete_item" -> if (isZh) "刪除條目" else "Delete Record"
            "edit_item" -> if (isZh) "修改條目" else "Edit Record"
            "select_report_month" -> if (isZh) "選擇財務報表月份" else "Select Monthly Report"
            "month_report_hint" -> if (isZh) "點擊月份即可切換查看該月份的詳細開銷、預算與財務分析圖表。" else "Select a month to view detailed expenses, budget, and charts."
            "report_summary" -> if (isZh) "報表概覽" else "Report Overview"
            "return_current_month" -> if (isZh) "回到當前月份" else "Return to Current Month"
            "viewing_history" -> if (isZh) "正在查看歷史報表" else "Viewing Historical Report"
            else -> key
        }
    }
}
