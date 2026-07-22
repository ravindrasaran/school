package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.TimetableViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(viewModel: TimetableViewModel) {
    // Premium organized roles: Admin Suite vs. Academic / Demo Suite
    val adminRoles = listOf(
        RoleItem("Principal", "Complete administrative controls, school settings, and overall insights", Icons.Default.SupervisorAccount, Color(0xFF38BDF8)), // Bright Cyan
        RoleItem("Vice Principal", "Manage teacher workloads, daily leaves, and substitute calendars", Icons.Default.People, Color(0xFF2DD4BF)), // Bright Teal
        RoleItem("Timetable Admin", "Full grid control, edit subjects, class sections, and class timings", Icons.Default.SettingsSuggest, Color(0xFFFB923C)) // Bright Orange
    )

    val academicRoles = listOf(
        RoleItem("Teacher", "View personal schedule, request official leaves, and upload homework", Icons.Default.School, Color(0xFFC084FC)), // Bright Violet
        RoleItem("Student / Parent", "Check real-time class timetable, holiday list, and view homework", Icons.Default.MenuBook, Color(0xFFF472B6)), // Bright Pink
        RoleItem("Guest Demo", "Instantly explore G.S.S. Hadetar scheduling database & AI advisor", Icons.Default.AutoAwesome, Color(0xFF60A5FA)) // Bright Blue
    )

    var selectedTab by remember { mutableStateOf(0) } // 0 = Admin Suite, 1 = Academic & Demo

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19)) // Solid slate dark base
            .drawBehind {
                // Drawing professional high-end ambient radial glow highlights
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.25f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.15f),
                        radius = size.width * 0.9f
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.8f, size.height * 0.15f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF4C1D95).copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.85f),
                        radius = size.width * 1.0f
                    ),
                    radius = size.width * 1.0f,
                    center = Offset(size.width * 0.1f, size.height * 0.85f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // 1. Made In India Pride Badge & App Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Dignified Pride Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF1E293B).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFFFF9933), CircleShape)) // Saffron
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape)) // White
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF128807), CircleShape)) // Green
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NATIONAL SCHEDULING PORTAL",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Beautiful Glowing App Icon and Title
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF38BDF8).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .border(BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.25f)), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "App Logo",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "School Timetable",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Enterprise-Grade Academic Optimizer & AI Advisor",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. Custom Portal Suite Switcher Tabs (Admin vs Academic)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Tab Selection Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TabPill(
                        title = "ADMIN SUITE",
                        isSelected = selectedTab == 0,
                        icon = Icons.Default.AdminPanelSettings,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        title = "ACADEMIC SUITE",
                        isSelected = selectedTab == 1,
                        icon = Icons.Default.School,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Beautiful List of Selected Roles
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
                    }
                ) { targetState ->
                    val activeList = if (targetState == 0) adminRoles else academicRoles
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activeList.forEach { role ->
                            PremiumRoleCard(
                                role = role,
                                onClick = {
                                    viewModel.selectRole(role.name)
                                }
                            )
                        }
                    }
                }
            }

            // 3. High-End Enterprise Capability Ticker & Info Badges
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    FeatureChip(text = "⚡ AI Optimization", color = Color(0xFFFB923C))
                    FeatureChip(text = "🔒 Fully Offline", color = Color(0xFF2DD4BF))
                    FeatureChip(text = "✓ M3 Material", color = Color(0xFF38BDF8))
                }
                
                Text(
                    text = "Aesthetic Platform for Government & Private Institutions of India",
                    color = Color(0xFF475569),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TabPill(
    title: String,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color.Transparent
    val borderStroke = if (isSelected) BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)) else null
    val textColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(8.dp)) else Modifier)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
fun PremiumRoleCard(role: RoleItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B).copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("role_card_${role.name.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing circular avatar placeholder for the icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(role.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, role.color.copy(alpha = 0.2f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = role.icon,
                    contentDescription = role.name,
                    tint = role.color,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = role.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = role.description,
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Clean active role indicator arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Proceed",
                tint = role.color.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FeatureChip(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFF1E293B).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color(0xFF64748B),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp
        )
    }
}

data class RoleItem(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)
