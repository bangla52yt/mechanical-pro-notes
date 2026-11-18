package com.researchbangla.mechanicalpronotes.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.researchbangla.mechanicalpronotes.di.AppContainer
import com.researchbangla.mechanicalpronotes.domain.catalog.FundamentalsCatalog
import com.researchbangla.mechanicalpronotes.domain.catalog.TechnicalReferenceCatalog
import com.researchbangla.mechanicalpronotes.domain.model.BookmarkEntityType
import com.researchbangla.mechanicalpronotes.domain.model.UserPreferences
import com.researchbangla.mechanicalpronotes.presentation.boiler.BoilerBookmarkTarget
import com.researchbangla.mechanicalpronotes.presentation.boiler.BoilerDashboardScreen
import com.researchbangla.mechanicalpronotes.presentation.boiler.BoilerViewModel
import com.researchbangla.mechanicalpronotes.presentation.boiler.boilerSectionBookmarkId
import com.researchbangla.mechanicalpronotes.presentation.boiler.resolveBoilerBookmarkTarget
import com.researchbangla.mechanicalpronotes.presentation.calculations.CalculationListScreen
import com.researchbangla.mechanicalpronotes.presentation.calculations.CalculationRunnerScreen
import com.researchbangla.mechanicalpronotes.presentation.calculations.CalculationViewModel
import com.researchbangla.mechanicalpronotes.presentation.calculations.SteamTableScreen
import com.researchbangla.mechanicalpronotes.presentation.checklists.ChecklistScreen
import com.researchbangla.mechanicalpronotes.presentation.checklists.ChecklistViewModel
import com.researchbangla.mechanicalpronotes.presentation.checklists.ChecklistWorkflowHelper
import com.researchbangla.mechanicalpronotes.presentation.equipment.EquipmentDetailScreen
import com.researchbangla.mechanicalpronotes.presentation.equipment.EquipmentListScreen
import com.researchbangla.mechanicalpronotes.presentation.equipment.EquipmentViewModel
import com.researchbangla.mechanicalpronotes.presentation.fundamentals.FundamentalsScreen
import com.researchbangla.mechanicalpronotes.presentation.home.HomeScreen
import com.researchbangla.mechanicalpronotes.presentation.home.HomeViewModel
import com.researchbangla.mechanicalpronotes.presentation.library.ReferenceLibraryScreen
import com.researchbangla.mechanicalpronotes.presentation.notes.BookmarksScreen
import com.researchbangla.mechanicalpronotes.presentation.notes.NotesScreen
import com.researchbangla.mechanicalpronotes.presentation.notes.NotesViewModel
import com.researchbangla.mechanicalpronotes.presentation.search.SearchScreen
import com.researchbangla.mechanicalpronotes.presentation.search.SearchViewModel
import com.researchbangla.mechanicalpronotes.presentation.settings.SettingsScreen
import com.researchbangla.mechanicalpronotes.presentation.settings.SettingsViewModel
import com.researchbangla.mechanicalpronotes.presentation.theme.MechanicalProTheme
import com.researchbangla.mechanicalpronotes.presentation.theme.ThemeMode
import com.researchbangla.mechanicalpronotes.presentation.units.UnitConverterScreen
import com.researchbangla.mechanicalpronotes.presentation.units.UnitConverterStateHolder
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val homeViewModel = remember {
        HomeViewModel(
            appContainer.equipmentRepository,
            appContainer.boilerRepository,
            appContainer.userDataRepository
        )
    }
    val equipmentViewModel = remember {
        EquipmentViewModel(appContainer.equipmentRepository, appContainer.userDataRepository)
    }
    val boilerViewModel = remember {
        BoilerViewModel(appContainer.boilerRepository)
    }
    val calculationViewModel = remember {
        CalculationViewModel(
            calculationRepository = appContainer.calculationRepository,
            userDataRepository = appContainer.userDataRepository,
            userPreferencesRepository = appContainer.userPreferencesRepository
        )
    }
    val searchViewModel = remember {
        SearchViewModel(appContainer.searchRepository)
    }
    val notesViewModel = remember {
        NotesViewModel(
            userDataRepository = appContainer.userDataRepository,
            equipmentRepository = appContainer.equipmentRepository,
            onNotesMutated = { searchViewModel.refreshSearchIfActive() }
        )
    }
    val checklistViewModel = remember {
        ChecklistViewModel(
            userDataRepository = appContainer.userDataRepository,
            equipmentRepository = appContainer.equipmentRepository
        )
    }
    val settingsViewModel = remember {
        SettingsViewModel(
            userDataRepository = appContainer.userDataRepository,
            userPreferencesRepository = appContainer.userPreferencesRepository,
            onPersonalDataCleared = { searchViewModel.refreshSearchIfActive() }
        )
    }
    val unitConverterStateHolder = remember {
        UnitConverterStateHolder()
    }
    val userPrefs by appContainer.userPreferencesRepository.userPreferences.collectAsState(
        initial = UserPreferences()
    )
    LaunchedEffect(userPrefs.unitSystem) {
        unitConverterStateHolder.applyUnitSystemPreference(userPrefs.unitSystem)
    }

    var pendingReferenceFocusId by remember { mutableStateOf<String?>(null) }
    var pendingFundamentalsFocusId by remember { mutableStateOf<String?>(null) }
    val notesState by notesViewModel.uiState.collectAsState()
    val bookmarkedBoilerSectionIds = remember(notesState.bookmarks) {
        notesState.bookmarks
            .asSequence()
            .filter { it.entityType == BookmarkEntityType.BOILER_SECTION }
            .map { it.entityId.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
    val bookmarkedReferenceIds = remember(notesState.bookmarks) {
        notesState.bookmarks
            .asSequence()
            .filter {
                it.entityType == BookmarkEntityType.TECHNICAL_STANDARD &&
                    !it.entityId.trim().startsWith("FUND-", ignoreCase = true)
            }
            .map { it.entityId.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
    val bookmarkedFundamentalIds = remember(notesState.bookmarks) {
        notesState.bookmarks
            .asSequence()
            .filter {
                it.entityType == BookmarkEntityType.TECHNICAL_STANDARD &&
                    it.entityId.trim().startsWith("FUND-", ignoreCase = true)
            }
            .map { it.entityId.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    val navigateToBookmarkTarget: (String, BookmarkEntityType) -> Unit = { rawEntityId, entityType ->
        val entityId = rawEntityId.trim()
        if (entityId.isNotEmpty()) {
            when (entityType) {
                BookmarkEntityType.EQUIPMENT -> {
                    navController.navigate(Screen.EquipmentDetail.createRoute(entityId.uppercase()))
                }
                BookmarkEntityType.BOILER_SECTION -> {
                    when (val target = resolveBoilerBookmarkTarget(entityId)) {
                        is BoilerBookmarkTarget.Tab -> {
                            boilerViewModel.selectTab(target.tab)
                            navController.navigate(Screen.BoilerModule.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                            }
                        }
                        is BoilerBookmarkTarget.Equipment -> {
                            navController.navigate(Screen.EquipmentDetail.createRoute(target.equipmentId))
                        }
                    }
                }
                BookmarkEntityType.CALCULATION -> {
                    navController.navigate(Screen.CalculationRunner.createRoute(entityId.uppercase()))
                }
                BookmarkEntityType.CHECKLIST -> {
                    checklistViewModel.clearEquipmentFilter()
                    checklistViewModel.selectChecklist(entityId)
                    navController.navigate(Screen.Checklists.route) {
                        launchSingleTop = true
                    }
                }
                BookmarkEntityType.TECHNICAL_STANDARD -> {
                    when (val target = resolveTechnicalStandardBookmarkTarget(entityId)) {
                        is TechnicalStandardBookmarkTarget.Fundamental -> {
                            pendingFundamentalsFocusId = target.topicId
                            navController.navigate(Screen.Fundamentals.route) {
                                launchSingleTop = true
                            }
                        }
                        is TechnicalStandardBookmarkTarget.Reference -> {
                            pendingReferenceFocusId = target.referenceId
                            navController.navigate(Screen.Library.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
        }
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.widthIn(min = 260.dp, max = 304.dp),
                windowInsets = DrawerDefaults.windowInsets
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "Mechanical Pro Notes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Personal Plant Reference (v1.0.0)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "100% Offline • 64 Assets • 20 Formulas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "PLANT MODULES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    Screen.drawerScreens.forEach { screen ->
                        NavigationDrawerItem(
                            icon = { Icon(getScreenIcon(screen), contentDescription = null) },
                            label = {
                                Column {
                                    Text(screen.title, fontWeight = FontWeight.Medium)
                                    Text(
                                        screen.bengaliTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            },
                            selected = currentRoute == screen.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (screen == Screen.Library) pendingReferenceFocusId = null
                                if (screen == Screen.Fundamentals) pendingFundamentalsFocusId = null
                                if (screen == Screen.Checklists) checklistViewModel.clearEquipmentFilter()
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )

                        if (screen == Screen.Etp || screen == Screen.Calculations) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))

                    Text(
                        text = "SPECIALIZED PLANT FAMILIES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    Screen.dedicatedFamilyDestinations.forEach { familyDest ->
                        NavigationDrawerItem(
                            icon = { Icon(getFamilyDestinationIcon(familyDest.familyId), contentDescription = null) },
                            label = {
                                Column {
                                    Text(
                                        text = familyDest.drawerTitle,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${familyDest.familyId} • ${familyDest.bengaliTitle}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            selected = currentRoute == familyDest.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                if (currentRoute != familyDest.route) {
                                    navController.navigate(familyDest.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))

                    Text(
                        text = "PERSONAL LOGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    listOf(Screen.Notes, Screen.Bookmarks).forEach { personalScreen ->
                        NavigationDrawerItem(
                            icon = { Icon(getScreenIcon(personalScreen), contentDescription = null) },
                            label = {
                                Column {
                                    Text(personalScreen.title, fontWeight = FontWeight.Medium)
                                    Text(
                                        personalScreen.bengaliTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            },
                            selected = currentRoute == personalScreen.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                val target = if (personalScreen == Screen.Notes) {
                                    Screen.Notes.createRoute(null)
                                } else {
                                    personalScreen.route
                                }
                                navController.navigate(target) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        val showBottomBar = currentRoute in listOf(
            Screen.Home.route,
            Screen.WashPlant.route,
            Screen.BoilerModule.route,
            Screen.Calculations.route
        )

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        Screen.bottomBarScreens.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(getScreenIcon(screen), contentDescription = screen.title) },
                                label = {
                                    Text(
                                        text = screen.shortTitle,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                selected = currentRoute == screen.route,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                        onNavigateToEquipment = { navController.navigate(Screen.WashPlant.route) },
                        onNavigateToBoiler = { navController.navigate(Screen.BoilerModule.route) },
                        onNavigateToCalculations = { navController.navigate(Screen.Calculations.route) },
                        onNavigateToEquipmentDetail = { eqId ->
                            navController.navigate(Screen.EquipmentDetail.createRoute(eqId))
                        },
                        onNavigateToUnitConverter = { navController.navigate(Screen.UnitConverter.route) },
                        onNavigateToFundamentals = {
                            pendingFundamentalsFocusId = null
                            navController.navigate(Screen.Fundamentals.route)
                        },
                        onNavigateToChecklists = {
                            checklistViewModel.clearEquipmentFilter()
                            navController.navigate(Screen.Checklists.route)
                        },
                        onNavigateToNotes = { navController.navigate(Screen.Notes.createRoute(null)) },
                        onNavigateToBookmarks = { navController.navigate(Screen.Bookmarks.route) },
                        onNavigateToSteamTable = { navController.navigate(Screen.SteamTable.route) },
                        onNavigateToBookmark = navigateToBookmarkTarget,
                        onRemoveBookmark = homeViewModel::removeBookmark,
                        onToggleEquipmentBookmark = homeViewModel::toggleEquipmentBookmark,
                        onNavigateToActiveChecklist = { checklistId ->
                            checklistViewModel.clearEquipmentFilter()
                            checklistViewModel.selectChecklist(checklistId)
                            navController.navigate(Screen.Checklists.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCalculation = { calcId ->
                            navController.navigate(Screen.CalculationRunner.createRoute(calcId.trim().uppercase()))
                        }
                    )
                }

                composable(Screen.WashPlant.route) {
                    EquipmentListScreen(
                        viewModel = equipmentViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                        onNavigateToDetail = { eqId ->
                            navController.navigate(Screen.EquipmentDetail.createRoute(eqId))
                        }
                    )
                }

                composable(Screen.BoilerModule.route) {
                    val calculationListState by calculationViewModel.listState.collectAsState()
                    BoilerDashboardScreen(
                        viewModel = boilerViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToEquipmentDetail = { eqId ->
                            navController.navigate(Screen.EquipmentDetail.createRoute(eqId))
                        },
                        onNavigateToCalculation = { calcId ->
                            navController.navigate(Screen.CalculationRunner.createRoute(calcId))
                        },
                        bookmarkedSectionIds = bookmarkedBoilerSectionIds,
                        onToggleSectionBookmark = { tab ->
                            notesViewModel.toggleBookmark(
                                entityType = BookmarkEntityType.BOILER_SECTION,
                                entityId = boilerSectionBookmarkId(tab),
                                title = "Boiler: ${tab.title}"
                            )
                        },
                        calculations = calculationListState.calculations,
                        onNavigateToSteamTable = {
                            navController.navigate(Screen.SteamTable.route)
                        },
                        unitSystem = userPrefs.unitSystem
                    )
                }

                composable(Screen.Calculations.route) {
                    CalculationListScreen(
                        viewModel = calculationViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToRunner = { calcId ->
                            navController.navigate(Screen.CalculationRunner.createRoute(calcId))
                        },
                        onNavigateToSteamTable = {
                            navController.navigate(Screen.SteamTable.route)
                        },
                        onNavigateToUnitConverter = {
                            navController.navigate(Screen.UnitConverter.route)
                        }
                    )
                }

                composable(Screen.SteamTable.route) {
                    SteamTableScreen(
                        onNavigateBack = { navController.popBackStack() },
                        unitSystem = userPrefs.unitSystem
                    )
                }

                composable(Screen.UnitConverter.route) {
                    UnitConverterScreen(
                        stateHolder = unitConverterStateHolder,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }

                composable(Screen.Fundamentals.route) {
                    FundamentalsScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToEquipmentDetail = { eqId ->
                            navController.navigate(Screen.EquipmentDetail.createRoute(eqId))
                        },
                        onNavigateToCalculation = { calcId ->
                            navController.navigate(Screen.CalculationRunner.createRoute(calcId))
                        },
                        onNavigateToLibrary = {
                            pendingReferenceFocusId = null
                            navController.navigate(Screen.Library.route)
                        },
                        initialFocusTopicId = pendingFundamentalsFocusId,
                        bookmarkedTopicIds = bookmarkedFundamentalIds,
                        onToggleBookmark = { topic ->
                            notesViewModel.toggleBookmark(
                                entityType = BookmarkEntityType.TECHNICAL_STANDARD,
                                entityId = topic.id,
                                title = topic.title
                            )
                        }
                    )
                }

                composable(
                    route = Screen.EquipmentDetail.route,
                    arguments = listOf(navArgument("equipmentId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val eqId = backStackEntry.arguments?.getString("equipmentId") ?: "EQ-A01"
                    val checklistState by checklistViewModel.uiState.collectAsState()
                    val detailState by equipmentViewModel.detailState.collectAsState()
                    val activeEquipment = detailState.equipment
                    val relevantChecklists = remember(
                        checklistState.checklists,
                        checklistState.executionMetadataByChecklist,
                        activeEquipment
                    ) {
                        if (activeEquipment == null) {
                            emptyList()
                        } else {
                            ChecklistWorkflowHelper.filterRelevantChecklistsForEquipment(
                                checklists = checklistState.checklists,
                                executionMetadata = checklistState.executionMetadataByChecklist,
                                equipment = activeEquipment
                            )
                        }
                    }
                    val checklistSummaries = remember(
                        relevantChecklists,
                        checklistState.completedByChecklist
                    ) {
                        relevantChecklists.associate { chk ->
                            chk.id to checklistState.progressSummaryFor(chk)
                        }
                    }
                    EquipmentDetailScreen(
                        equipmentId = eqId,
                        viewModel = equipmentViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToNoteEditor = { equipmentId ->
                            navController.navigate(Screen.Notes.createRoute(equipmentId))
                        },
                        onNavigateToCalculation = { calcId ->
                            navController.navigate(Screen.CalculationRunner.createRoute(calcId))
                        },
                        relevantChecklists = relevantChecklists,
                        checklistSummaries = checklistSummaries,
                        onOpenChecklistForEquipment = { targetEqId, preferredChecklistId ->
                            checklistViewModel.openForEquipment(targetEqId, preferredChecklistId)
                            navController.navigate(Screen.Checklists.route)
                        },
                        onCreateCustomChecklistForEquipment = { targetEqId ->
                            checklistViewModel.openForEquipment(targetEqId, null)
                            checklistViewModel.openCreateCustomChecklist(targetEqId)
                            navController.navigate(Screen.Checklists.route)
                        }
                    )
                }

                composable(
                    route = Screen.CalculationRunner.route,
                    arguments = listOf(navArgument("calcId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val calcId = backStackEntry.arguments?.getString("calcId") ?: "CALC-01"
                    CalculationRunnerScreen(
                        calculationId = calcId,
                        viewModel = calculationViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSteamTable = {
                            navController.navigate(Screen.SteamTable.route)
                        }
                    )
                }

                composable(Screen.Search.route) {
                    SearchScreen(
                        viewModel = searchViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEquipmentDetail = { eqId ->
                            navController.navigate(Screen.EquipmentDetail.createRoute(eqId))
                        },
                        onNavigateToChecklist = { chkId ->
                            checklistViewModel.selectChecklist(chkId)
                            navController.navigate(Screen.Checklists.route)
                        },
                        onNavigateToReference = { refId ->
                            pendingReferenceFocusId = refId
                            navController.navigate(Screen.Library.route)
                        },
                        onNavigateToFundamentals = { fundId ->
                            pendingFundamentalsFocusId = fundId
                            navController.navigate(Screen.Fundamentals.route)
                        },
                        onNavigateToCalculation = { calcId ->
                            navController.navigate(Screen.CalculationRunner.createRoute(calcId))
                        },
                        onNavigateToUserNote = { noteId, equipmentId ->
                            notesViewModel.openNoteById(noteId, equipmentId)
                            navController.navigate(Screen.Notes.createRoute(null)) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToCalculationHistory = { historyId ->
                            calculationViewModel.focusHistoryRecord(historyId)
                            navController.navigate(Screen.Calculations.route) {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToBookmarks = {
                            navController.navigate(Screen.Bookmarks.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(
                    route = Screen.Notes.route,
                    arguments = listOf(navArgument("equipmentId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val eqIdArg = backStackEntry.arguments?.getString("equipmentId")
                    NotesScreen(
                        viewModel = notesViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToEquipment = { eqId ->
                            navController.navigate(Screen.EquipmentDetail.createRoute(eqId))
                        },
                        initialEquipmentId = eqIdArg
                    )
                }

                composable(Screen.Bookmarks.route) {
                    BookmarksScreen(
                        viewModel = notesViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToItem = navigateToBookmarkTarget
                    )
                }

                composable(Screen.Checklists.route) {
                    ChecklistScreen(
                        viewModel = checklistViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }

                composable(Screen.Library.route) {
                    ReferenceLibraryScreen(
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        initialFocusRefId = pendingReferenceFocusId,
                        bookmarkedRefIds = bookmarkedReferenceIds,
                        onToggleBookmark = { ref ->
                            notesViewModel.toggleBookmark(
                                entityType = BookmarkEntityType.TECHNICAL_STANDARD,
                                entityId = ref.id,
                                title = ref.title
                            )
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }

                val secondaryModules = listOf(
                    Screen.Pumps,
                    Screen.Transmission,
                    Screen.Steam,
                    Screen.Water,
                    Screen.Pneumatics,
                    Screen.CoolingHeating,
                    Screen.Etp,
                    Screen.Maintenance
                )

                secondaryModules.forEach { moduleScreen ->
                    composable(moduleScreen.route) {
                        val equipmentListState by equipmentViewModel.listState.collectAsState()
                        val calculationListState by calculationViewModel.listState.collectAsState()
                        SecondaryModuleScreen(
                            screen = moduleScreen,
                            equipmentListState = equipmentListState,
                            calculations = calculationListState.calculations,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToEquipmentDetail = { eqId ->
                                navController.navigate(Screen.EquipmentDetail.createRoute(eqId))
                            },
                            onBrowseFamilyInWashPlant = { familyId ->
                                equipmentViewModel.selectFamily(familyId)
                                navController.navigate(Screen.WashPlant.route)
                            },
                            onOpenCalculator = { calcId ->
                                navController.navigate(Screen.CalculationRunner.createRoute(calcId))
                            },
                            onOpenSteamTable = {
                                navController.navigate(Screen.SteamTable.route)
                            },
                            onOpenUnitConverter = {
                                navController.navigate(Screen.UnitConverter.route)
                            },
                            onNavigateToReference = { refId ->
                                pendingReferenceFocusId = refId
                                navController.navigate(Screen.Library.route)
                            },
                            onNavigateToFundamental = { fundId ->
                                pendingFundamentalsFocusId = fundId
                                navController.navigate(Screen.Fundamentals.route)
                            },
                            onNavigateToChecklist = { chkId ->
                                checklistViewModel.selectChecklist(chkId)
                                navController.navigate(Screen.Checklists.route)
                            },
                            onNavigateToNotes = {
                                navController.navigate(Screen.Notes.createRoute(null))
                            },
                            onNavigateToCalculations = {
                                navController.navigate(Screen.Calculations.route)
                            },
                            onNavigateToFamilyDestination = { familyId ->
                                Screen.findFamilyDestinationById(familyId)?.let { dest ->
                                    navController.navigate(dest.route)
                                }
                            }
                        )
                    }
                }

                Screen.dedicatedFamilyDestinations.forEach { familyDest ->
                    composable(familyDest.route) {
                        val equipmentListState by equipmentViewModel.listState.collectAsState()
                        EquipmentFamilyDestinationScreen(
                            destination = familyDest,
                            equipmentListState = equipmentListState,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onNavigateToEquipmentDetail = { eqId ->
                                navController.navigate(Screen.EquipmentDetail.createRoute(eqId))
                            },
                            onOpenInFullDirectory = { famId ->
                                equipmentViewModel.selectFamily(famId)
                                navController.navigate(Screen.WashPlant.route)
                            },
                            onNavigateToReference = { refId ->
                                pendingReferenceFocusId = refId
                                navController.navigate(Screen.Library.route)
                            },
                            onNavigateToFundamental = { fundId ->
                                pendingFundamentalsFocusId = fundId
                                navController.navigate(Screen.Fundamentals.route)
                            }
                        )
                    }
                }
            }
        }
    }
}

sealed interface TechnicalStandardBookmarkTarget {
    data class Reference(val referenceId: String) : TechnicalStandardBookmarkTarget
    data class Fundamental(val topicId: String) : TechnicalStandardBookmarkTarget
}

fun resolveTechnicalStandardBookmarkTarget(entityId: String): TechnicalStandardBookmarkTarget {
    val trimmed = entityId.trim()
    val matchedFundamental = FundamentalsCatalog.ALL_TOPICS.firstOrNull {
        it.id.equals(trimmed, ignoreCase = true)
    }
    if (matchedFundamental != null || trimmed.startsWith("FUND-", ignoreCase = true)) {
        return TechnicalStandardBookmarkTarget.Fundamental(matchedFundamental?.id ?: trimmed.uppercase())
    }
    val matchedReference = TechnicalReferenceCatalog.ALL_REFERENCES.firstOrNull {
        it.id.equals(trimmed, ignoreCase = true)
    }
    return TechnicalStandardBookmarkTarget.Reference(matchedReference?.id ?: trimmed.uppercase())
}

private fun getScreenIcon(screen: Screen): ImageVector {
    return when (screen) {
        Screen.Home -> Icons.Default.Home
        Screen.Fundamentals -> Icons.Default.MenuBook
        Screen.WashPlant -> Icons.Default.LocalLaundryService
        Screen.BoilerModule -> Icons.Default.LocalFireDepartment
        Screen.Pumps -> Icons.Default.WaterDrop
        Screen.Transmission -> Icons.Default.Settings
        Screen.Steam -> Icons.Default.Air
        Screen.Water -> Icons.Default.Opacity
        Screen.Pneumatics -> Icons.Default.Compress
        Screen.CoolingHeating -> Icons.Default.AcUnit
        Screen.Etp -> Icons.Default.Recycling
        Screen.Maintenance -> Icons.Default.Build
        Screen.Checklists -> Icons.Default.Checklist
        Screen.Calculations -> Icons.Default.Calculate
        Screen.UnitConverter -> Icons.Default.SwapHoriz
        Screen.Library -> Icons.Default.LibraryBooks
        Screen.Search -> Icons.Default.Search
        Screen.Settings -> Icons.Default.SettingsSuggest
        Screen.Notes -> Icons.Default.NoteAlt
        Screen.Bookmarks -> Icons.Default.Bookmark
        else -> Icons.Default.Circle
    }
}

private fun getFamilyDestinationIcon(familyId: String): ImageVector {
    return when (familyId.uppercase()) {
        "FAM-C" -> Icons.Default.DryCleaning
        "FAM-I" -> Icons.Default.Science
        "FAM-M" -> Icons.Default.PrecisionManufacturing
        "FAM-N" -> Icons.Default.Handyman
        "FAM-O" -> Icons.Default.HealthAndSafety
        "FAM-P" -> Icons.Default.Air
        else -> Icons.Default.Category
    }
}

@Preview(name = "SecondaryModule - Steam Light", showBackground = true, widthDp = 380)
@Composable
private fun SecondaryModuleOverviewLightPreview() {
    MechanicalProTheme(themeMode = ThemeMode.LIGHT) {
        SecondaryModuleContent(
            screen = Screen.Steam,
            resolved = SecondaryModuleResolver.resolveModule(
                screen = Screen.Steam,
                allEquipment = emptyList(),
                systemFamilies = emptyList()
            ),
            onOpenDrawer = {}
        )
    }
}

@Preview(name = "SecondaryModule - ETP Dark", showBackground = true, widthDp = 380)
@Composable
private fun SecondaryModuleOverviewDarkPreview() {
    MechanicalProTheme(themeMode = ThemeMode.DARK) {
        SecondaryModuleContent(
            screen = Screen.Etp,
            resolved = SecondaryModuleResolver.resolveModule(
                screen = Screen.Etp,
                allEquipment = emptyList(),
                systemFamilies = emptyList()
            ),
            onOpenDrawer = {}
        )
    }
}
