import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {
    Box,
    ToggleButtonGroup,
    ToggleButton,
    IconButton,
    Menu,
    MenuItem,
    Checkbox,
    ListItemIcon,
    ListItemText,
    Button,
    Tooltip,
    useTheme,
    Divider
} from "@mui/material";
import CalendarTodayOutlinedIcon from '@mui/icons-material/CalendarTodayOutlined';
import CalendarMonthOutlinedIcon from '@mui/icons-material/CalendarMonthOutlined';
import AddCircleOutlineOutlinedIcon from '@mui/icons-material/AddCircleOutlineOutlined';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import FilterListOutlinedIcon from '@mui/icons-material/FilterListOutlined';
import { useCalendarContext } from "../../context/CalendarContext";
import { useCategoryContext} from "../../context/CategoryContext";
import CreateCategoryDialog from "../CreateCategoryDialog";
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined';
import RadioButtonUncheckedOutlinedIcon from '@mui/icons-material/RadioButtonUncheckedOutlined';
import CancelOutlinedIcon from '@mui/icons-material/CancelOutlined';
import { grey } from "@mui/material/colors";
import {useTodoContext} from "../../context/TodoContext";

export default function TopBar() {
    const theme = useTheme();
    const navigate = useNavigate();
    const location = useLocation();
    const isCalendarPage = location.pathname === "/calendar";

    const {
        currentView,
        setCurrentView,
        openAddDialog,
    } = useCalendarContext();

    const {
        categories,
        selectedCategories,
        toggleCategory,
        resetFilter,
        createCategory,
        useCategoryDialog
    } = useCategoryContext();

    const { open, handleOpen, handleClose, handleCreate } = useCategoryDialog();


    const [openCreateCategoryDialog, setOpenCreateCategoryDialog] = useState(false);
    const [filterAnchorEl, setFilterAnchorEl] = useState(null);

    const [addAnchorEl, setAddAnchorEl] = useState(null);

    const handleFilterOpen = (e) => setFilterAnchorEl(e.currentTarget);
    const handleFilterClose = () => setFilterAnchorEl(null);
    const handleViewChange = (_e, nextView) => nextView && setCurrentView(nextView);

    const handleAddMenuOpen = (e) => setAddAnchorEl(e.currentTarget);
    const handleAddMenuClose = () => setAddAnchorEl(null);

    const goAddActivity = () => {
        handleAddMenuClose();
        navigate("/calendar");
        openAddDialog();
    };

    const { openTodoDialog } = useTodoContext()

    const goAddTask = () => {
        handleAddMenuClose();
        navigate("/Todo");
        openTodoDialog();
    };

    return (
        <Box
            component="div"
            sx={{
                display: 'flex',
                alignItems: 'center',
                px: 2,
                py: 0,
                borderBottom: `1px solid ${theme.palette.divider}`,
                backgroundColor: theme.palette.background.paper,
                gap: 2,
                overflowX: 'auto'
            }}
        >
            {isCalendarPage && (
                <>
                    {/* View toggle */}
                    <ToggleButtonGroup
                        value={currentView}
                        exclusive
                        onChange={handleViewChange}
                        size="small"
                    >
                        <ToggleButton value="week" aria-label="Veckovy">
                            <CalendarTodayOutlinedIcon fontSize="small" />
                            <Box component="span" sx={{ ml: 0.5 }}>Veckovy</Box>
                        </ToggleButton>
                        <ToggleButton value="month" aria-label="Månadsvy">
                            <CalendarMonthOutlinedIcon fontSize="small" />
                            <Box component="span" sx={{ ml: 0.5 }}>Månadsvy</Box>
                        </ToggleButton>
                    </ToggleButtonGroup>

                    <Divider orientation="vertical" flexItem sx={{ mx: 1 }} />

                    {/* Categories filter menu */}
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <Tooltip title="Filter Categories" arrow>
                            <IconButton onClick={handleFilterOpen}>
                                <FilterListOutlinedIcon />
                            </IconButton>
                        </Tooltip>
                        <Menu
                            anchorEl={filterAnchorEl}
                            open={Boolean(filterAnchorEl)}
                            onClose={handleFilterClose}
                        >
                            <MenuItem
                                onClick={resetFilter}
                                sx={{
                                    color: grey[600],
                                }}>
                                <ListItemIcon>
                                    <CancelOutlinedIcon fontSize="small" />
                                </ListItemIcon>
                                <ListItemText primary="Rensa filter" />
                            </MenuItem>
                            <Divider />
                            {categories.map((cat) => (
                                <MenuItem key={cat.id} onClick={() => toggleCategory(cat.id)}>
                                    <IconButton sx={{ p: 0 }}>
                                        <Checkbox
                                            icon={<RadioButtonUncheckedOutlinedIcon sx={{ color: cat.color }} />}
                                            checkedIcon={<CheckCircleOutlinedIcon sx={{ color: cat.color }} />}
                                            checked={selectedCategories.includes(cat.id)}
                                            size="small"
                                            sx={{
                                                pl: 0,
                                                justifyContent: "flex-start",
                                                alignItems: "center",
                                                display: "flex",
                                            }}
                                        />
                                    </IconButton>
                                    <ListItemText primary={cat.name} />
                                </MenuItem>
                            ))}

                            <Divider />

                            <MenuItem
                                onClick={() => {
                                    handleOpen();
                                    handleFilterClose();
                                }}
                            >
                                <ListItemIcon>
                                    <AddCircleOutlineOutlinedIcon fontSize="small" />
                                </ListItemIcon>
                                <ListItemText primary="Skapa kategori" />
                            </MenuItem>
                        </Menu>
                        Filter
                    </Box>
                </>
            )}


            <Box sx={{ flexGrow: 1 }} />

            {/* Add Activity button with dropdown */}
            <Button
                variant="contained"
                color="primary"
                startIcon={<AddCircleOutlineOutlinedIcon />}
                endIcon={<ArrowDropDownIcon />}
                onClick={handleAddMenuOpen}
            >
                Lägg till
            </Button>
            <Menu
                anchorEl={addAnchorEl}
                open={Boolean(addAnchorEl)}
                onClose={handleAddMenuClose}
            >
                <MenuItem onClick={goAddActivity}>
                    <ListItemText primary="Skapa ny aktivitet" />
                </MenuItem>
                <MenuItem onClick={goAddTask}>
                    <ListItemText primary="Skapa ny ToDo" />
                </MenuItem>
            </Menu>

            <CreateCategoryDialog
                open={open}
                onClose={handleClose}
                onCreate={handleCreate}
            />
        </Box>
    );
}