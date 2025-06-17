// src/components/task/ToDoList.jsx
import React, { useState, useRef } from "react";
import {
    Box,
    Typography,
    Stack,
    TextField,
    IconButton,
    Menu,
    MenuItem,
    Tooltip,
    Card,
    CardContent,
    Checkbox,
    Collapse,
    Button,
    Accordion,
    AccordionSummary,
    AccordionDetails, Divider
} from "@mui/material";
import {
    ExpandMore as ExpandMoreIcon,
    DeleteOutlineOutlined as DeleteOutlineOutlinedIcon,
    Category as CategoryIcon,
    DateRange as DateRangeIcon,
} from "@mui/icons-material";
import { motion, AnimatePresence } from "framer-motion";
import Lottie from 'lottie-react';
import confettiAnimation from '../../animations/Confetti.json';
import { alpha } from "@mui/material/styles";
import { useTodoContext } from "../../context/TodoContext";
import { useCalendarContext } from "../../context/CalendarContext";
import { useCategoryContext } from "../../context/CategoryContext";
import CreateCategoryDialog from "../CreateCategoryDialog";
import AddTask from "./AddTask";
import ConfirmationDialog from "../ConfirmationDialog";
import AddActivity from "../calendar/AddActivity";
import dayjs from "dayjs";

export default function ToDoList() {
    const {
        tasks,
        loading,
        error,
        createTask,
        updateTask,
        deleteTask,
        toggleComplete,
        formData,
        setFormData,
        isDialogOpen,
        dialogMode: todoDialogMode,
        openTodoDialog,
        closeTodoDialog,
    } = useTodoContext();

    const {
        openAddDialog,
        isAddEditDialogOpen,
        handleCloseDialog,
        dialogMode: calendarDialogMode,
    } = useCalendarContext();

    const {
        categories,
        useCategoryDialog
    } = useCategoryContext();

    const { open, handleOpen, handleClose, handleCreate } = useCategoryDialog();

    const [selectedTaskId, setSelectedTaskId] = useState(null);
    const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
    const [taskToDelete, setTaskToDelete] = useState(null);
    const [sweepingTaskId, setSweepingTaskId] = useState(null);
    const [showConfetti, setShowConfetti] = useState(false);

    // Quick-add fields
    const [newTodoName, setNewTodoName] = useState("");
    const [newTodoDescription, setNewTodoDescription] = useState("");
    const [newTodoCategory, setNewTodoCategory] = useState("");
    const [catMenuAnchor, setCatMenuAnchor] = useState(null);
    const [newTodoDate, setNewTodoDate] = useState("");
    const dateInputRef = useRef(null);

    const handleToggleCompleted = async (task) => {
        setSweepingTaskId(task.id);

        if (!task.completed) {
            setShowConfetti(true);
        }

        setTimeout(async () => {
            await toggleComplete(task);
            setSweepingTaskId(null);
            setShowConfetti(false);
        }, 1000);
    };

    const incompleteTasks = tasks.filter((t) => !t.completed);
    const completedTasks = tasks.filter((t) => t.completed);

    const variants = {
        initial: { opacity: 0, x: -50 },
        animate: { opacity: 1, x: 0 },
        exit: { opacity: 0, x: 50 },
    };

    const handleCardClick = (taskId) => {
        setSelectedTaskId((prev) => (prev === taskId ? null : taskId));
    };

    return (
        <Box sx={{ p: 2, display: "flex", flexDirection: "column" }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                <Typography variant="h1">ToDo</Typography>
            </Box>

            {/* Quick-add ToDo */}
            <Box mb={2}>
                <TextField
                    size="small"
                    label="Lägg till en ny ToDo"
                    placeholder="Titel"
                    value={newTodoName}
                    onChange={(e) => setNewTodoName(e.target.value)}
                    onKeyDown={async (e) => {
                        if (e.key === "Enter" && newTodoName.trim()) {
                            try {
                                await createTask({
                                    name: newTodoName.trim(),
                                    description: newTodoDescription.trim() || undefined,
                                    categoryId: newTodoCategory || undefined,
                                    date: newTodoDate || undefined,
                                });
                                setNewTodoName("");
                                setNewTodoDescription("");
                                setNewTodoCategory("");
                                setNewTodoDate("");
                            } catch (err) {
                                console.error("Error creating task:", err);
                            }
                        }
                    }}
                    fullWidth
                />

                {newTodoName.trim() !== "" && (
                    <Box sx={{ display: "flex", alignItems: "center", gap: 1, mt: 1, ml: 2 }}>
                        <TextField
                            size="small"
                            sx={{ flex: 1 }}
                            label="Lägg till en beskrivning"
                            placeholder="Beskrivning"
                            value={newTodoDescription}
                            onChange={(e) => setNewTodoDescription(e.target.value)}
                            onKeyDown={async (e) => {
                                if (e.key === "Enter" && newTodoName.trim()) {
                                    try {
                                        await createTask({
                                            name: newTodoName.trim(),
                                            description: newTodoDescription.trim() || undefined,
                                            categoryId: newTodoCategory || undefined,
                                            date: newTodoDate || undefined,
                                        });
                                        setNewTodoName("");
                                        setNewTodoDescription("");
                                        setNewTodoCategory("");
                                        setNewTodoDate("");
                                    } catch (err) {
                                        console.error("Error creating task:", err);
                                    }
                                }
                            }}
                        />
                        <Tooltip title="Välj kategori" arrow>
                            <IconButton
                                onClick={(e) => setCatMenuAnchor(e.currentTarget)}
                                sx={{ color: newTodoCategory ? "primary.main" : "text.secondary" }}
                            >
                                <CategoryIcon />
                            </IconButton>
                        </Tooltip>
                        <Menu
                            anchorEl={catMenuAnchor}
                            open={Boolean(catMenuAnchor)}
                            onClose={() => setCatMenuAnchor(null)}
                        >
                            <MenuItem
                                onClick={() => {
                                    setNewTodoCategory("");
                                    setCatMenuAnchor(null);
                                }}
                            >
                                <em>Rensa</em>
                            </MenuItem>

                            <Divider />

                            {categories.map((cat) => (
                                <MenuItem
                                    key={cat.id}
                                    onClick={() => {
                                        setNewTodoCategory(cat.id);
                                        setCatMenuAnchor(null);
                                    }}
                                >
                                    <Box sx={{ display: "inline-flex", alignItems: "center", gap: 1 }}>
                                        <Box
                                            sx={{
                                                width: 12,
                                                height: 12,
                                                bgcolor: cat.color,
                                                borderRadius: "50%",
                                            }}
                                        />
                                        {cat.name}
                                    </Box>
                                </MenuItem>
                            ))}

                            <Divider />

                            <MenuItem onClick={handleOpen}>
                                <Box sx={{textAlign: "center", width: "100%" }}>
                                    Skapa ny kategori
                                </Box>
                            </MenuItem>
                        </Menu>
                        <Box sx={{ position: "relative" }}>
                            <Tooltip title="Välj datum" arrow>
                                <IconButton
                                    onClick={() => {
                                        if (dateInputRef.current?.showPicker) {
                                            dateInputRef.current.showPicker();
                                        } else {
                                            dateInputRef.current.click();
                                        }
                                    }}
                                    sx={{ color: newTodoDate ? "primary.main" : "text.secondary" }}
                                >
                                    <DateRangeIcon />
                                </IconButton>
                            </Tooltip>
                            <input
                                ref={dateInputRef}
                                type="date"
                                value={newTodoDate}
                                onChange={(e) => setNewTodoDate(e.target.value)}
                                style={{
                                    position: "absolute",
                                    top: 0,
                                    left: 0,
                                    width: "100%",
                                    height: "100%",
                                    opacity: 0,
                                    pointerEvents: "none"
                                }}
                            />
                        </Box>
                    </Box>
                )}
            </Box>
            <Box sx={{ display: "flex", flexDirection: "column", maxHeight: "37vh", minHeight: 0, overflowY: "auto", pb: 1 }}>
                {/* Task List */}
                <Stack spacing={2}>
                    {loading && <Typography>Loading…</Typography>}
                    {error && <Typography color="error">{error}</Typography>}

                    <AnimatePresence>
                        {incompleteTasks.sort((a, b) => a.date?.localeCompare(b.date)).map((task) => {
                                const cat = categories.find((c) => c.id === task.categoryId) || null;
                                return (
                                    <motion.div
                                        key={task.id}
                                        initial="initial"
                                        animate="animate"
                                        exit="exit"
                                        variants={variants}
                                        transition={{ duration: 0.3 }}
                                    >
                                        <Card>
                                            <CardContent
                                                onClick={() => handleCardClick(task.id)}
                                                sx={{
                                                    cursor: "pointer",
                                                    maxHeight: "65px",
                                                    minHeight: "65px",
                                                    display: "flex",
                                                    alignItems: "center",
                                                    position: "relative",
                                                    overflow: "hidden",
                                                    borderLeft: cat?.color ? `4px solid ${cat.color}` : "4px solid #ccc",
                                                    backgroundColor: cat?.color ? alpha(cat.color, 0.1) : "background.paper",
                                                    transition: "background-color 0.3s ease",
                                                    "&:hover": {
                                                        backgroundColor: cat?.color ? alpha(cat.color, 0.5) : "action.hover",
                                                    },
                                                }}
                                            >
                                                {sweepingTaskId === task.id && (
                                                    <Box
                                                        sx={{
                                                            position: "absolute",
                                                            top: 0,
                                                            left: "-100%",
                                                            width: "100%",
                                                            height: "100%",
                                                            bgcolor: cat?.color ? alpha(cat.color, 0.7) : alpha("#4BB543", 0.7 ),
                                                            animation: "sweep 0.5s ease-in forwards",
                                                            zIndex: 0,
                                                            "@keyframes sweep": {
                                                                "0%": {
                                                                    left: "-100%",
                                                                },
                                                                "100%": {
                                                                    left: 0,
                                                                },
                                                            },
                                                        }}
                                                    />
                                                )}

                                                <Checkbox
                                                    checked={task.completed}
                                                    onClick={(e) => e.stopPropagation()}
                                                    onChange={() => handleToggleCompleted(task)}
                                                    sx={{ position: "absolute", top: 2, right: 2, zIndex: 1 }}
                                                />
                                                <Box
                                                    sx={{
                                                        textDecoration: task.completed ? "line-through" : "none",
                                                        color: task.completed ? "text.disabled" : "text.primary",
                                                        whiteSpace: "pre-wrap",
                                                        wordBreak: "break-word",
                                                        zIndex: 1,
                                                    }}
                                                >
                                                    <Typography variant="h6">{task.name}</Typography>

                                                    {selectedTaskId !== task.id && (
                                                        <>
                                                            <Typography variant="body2">{task.description}</Typography>
                                                            {task.date && (
                                                                <Typography variant="caption">
                                                                    Ska utföras innan: {task.date}
                                                                </Typography>
                                                            )}
                                                        </>
                                                    )}
                                                </Box>
                                                {showConfetti && sweepingTaskId === task.id && (
                                                    <Box
                                                        sx={{
                                                            position: 'absolute',
                                                            top: '50%',
                                                            left: '50%',
                                                            transform: 'translate(-50%, -50%)',
                                                            width: 200,
                                                            height: 200,
                                                            pointerEvents: 'none',
                                                            zIndex: 10,
                                                        }}
                                                    >
                                                        <Lottie
                                                            animationData={confettiAnimation}
                                                            loop={false}
                                                            autoplay
                                                            style={{ width: '100%', height: '100%' }}
                                                        />
                                                    </Box>
                                                )}
                                            </CardContent>

                                            {/* Expandable Task Details */}
                                            <AnimatePresence initial={false}>
                                                {selectedTaskId === task.id && (
                                                    <motion.div
                                                        initial={{ height: 0, opacity: 0 }}
                                                        animate={{ height: "auto", opacity: 1 }}
                                                        exit={{ height: 0, opacity: 0 }}
                                                        transition={{ duration: 0.4, ease: "easeInOut" }}
                                                    >
                                                        <Box
                                                            sx={{
                                                                pt: 0.5,
                                                                pl: 2,
                                                                pb: 2,
                                                                borderLeft: cat?.color ? `4px solid ${cat.color}` : "4px solid #ccc",
                                                                backgroundColor: cat?.color ? alpha(cat.color, 0.1) : "background.paper",
                                                                position: "relative",
                                                            }}
                                                        >
                                                            <Typography variant="body2" sx={{ mb: 1 }}>
                                                                Beskrivning: {task.description || "Ingen beskrivning"}
                                                            </Typography>
                                                            <Typography variant="body2" sx={{ mb: 1 }}>
                                                                Plats: {task.location || "Ingen plats"}
                                                            </Typography>
                                                            <Typography variant="body2" sx={{ mb: 1 }}>
                                                                Datum: {task.date || "Inget datum"}
                                                            </Typography>

                                                            <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
                                                                <Button
                                                                    variant="contained"
                                                                    size="small"
                                                                    onClick={(e) => {
                                                                        e.stopPropagation();
                                                                        openTodoDialog("edit", task);
                                                                    }}
                                                                    sx={{
                                                                        backgroundColor: "primary.main",
                                                                        color: "primary.contrastText",
                                                                        "&:hover": {
                                                                            backgroundColor: "primary.light",
                                                                            color: "primary.contrastText",
                                                                        },
                                                                    }}
                                                                >
                                                                    Redigera
                                                                </Button>
                                                                <Button
                                                                    variant="contained"
                                                                    size="small"
                                                                    color="primary" onClick={(e) => {
                                                                    e.stopPropagation();
                                                                    openAddDialog(task, task.id);
                                                                    }}
                                                                    sx={{
                                                                        backgroundColor: "primary.main",
                                                                        color: "primary.contrastText",
                                                                        "&:hover": {
                                                                            backgroundColor: "primary.light",
                                                                            color: "primary.contrastText",
                                                                        },
                                                                    }}
                                                                >
                                                                    Konvertera till aktivitet
                                                                </Button>
                                                            </Stack>
                                                            <IconButton
                                                                onClick={(e) => {
                                                                    e.stopPropagation();
                                                                    if (task.completed) {
                                                                        deleteTask(task.id);
                                                                    } else {
                                                                        setTaskToDelete(task);
                                                                        setConfirmDeleteOpen(true);
                                                                    }
                                                                }}
                                                                sx={{
                                                                    position: "absolute",
                                                                    bottom: 8,
                                                                    right: 8,
                                                                    color: "error.main",
                                                                }}
                                                            >
                                                                <DeleteOutlineOutlinedIcon />
                                                            </IconButton>
                                                        </Box>
                                                    </motion.div>
                                                )}
                                            </AnimatePresence>
                                        </Card>
                                    </motion.div>
                                );
                            })}
                    </AnimatePresence>
                </Stack>

                {/* Dialogs */}
                <AddTask
                    open={isDialogOpen}
                    onClose={closeTodoDialog}
                    formData={formData}
                    handleChange={(e) => {
                        const { name, value } = e.target;
                        setFormData((prev) => ({ ...prev, [name]: value }));
                    }}
                    handleSubmit={async () => {
                        if (todoDialogMode === "add") {
                            await createTask(formData);
                        } else {
                            await updateTask(formData.id, formData);
                        }
                        closeTodoDialog();
                    }}
                />

                <ConfirmationDialog
                    open={confirmDeleteOpen}
                    onClose={() => setConfirmDeleteOpen(false)}
                    onConfirm={async () => {
                        if (taskToDelete) {
                            await deleteTask(taskToDelete.id);
                            setConfirmDeleteOpen(false);
                            setTaskToDelete(null);
                        }
                    }}
                    title="Ta bort ToDo"
                    content="Är du säker på att du vill radera denna ToDo?"
                    buttonText="Radera"
                />

                <AddActivity
                    open={isAddEditDialogOpen}
                    mode={calendarDialogMode}
                    onClose={handleCloseDialog}
                />

            </Box>

            {/* Completed Tasks */}
            <Accordion
                sx={{
                    mt: 2,
                    border: '1px solid #ccc',
                    borderRadius: 2,
                    "&:hover": {
                        border: '1px solid',
                        borderColor: 'primary.main',
                    },
                }}
            >
                {loading && <Typography>Loading…</Typography>}
                {error && <Typography color="error">{error}</Typography>}

                <AccordionSummary expandIcon={<ExpandMoreIcon sx={{ color: 'primary.main'}} />}>
                    <Typography>Slutförda ({completedTasks.length})</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <Box sx={{ maxHeight: "20.5vh", overflowY: "auto" }}>
                        <Stack spacing={1}>
                            <AnimatePresence>
                                {completedTasks.map((task) => {
                                    const cat = categories.find((c) => c.id === task.categoryId) || null;
                                    return (
                                        <motion.div
                                            key={task.id}
                                            initial="initial"
                                            animate="animate"
                                            exit="exit"
                                            variants={variants}
                                            transition={{ duration: 0.3 }}
                                        >
                                            <Card variant="outlined">
                                                <CardContent
                                                    sx={{
                                                        maxHeight: "50px",
                                                        minHeight: "40px",
                                                        display: "flex",
                                                        alignItems: "center",
                                                        opacity: 0.7,
                                                        textDecoration: "line-through",
                                                        position: "relative",
                                                        borderLeft: cat?.color ? `4px solid ${cat.color}` : "4px solid transparent",
                                                        backgroundColor: cat?.color ? alpha(cat.color, 0.1) : "background.paper",
                                                    }}
                                                >
                                                    <Checkbox
                                                        checked={task.completed}
                                                        onChange={() => handleToggleCompleted(task)}
                                                        sx={{ position: "absolute", top: 2, right: 2 }}
                                                    />
                                                    <IconButton
                                                        onClick={() => deleteTask(task.id)}
                                                        sx={{ position: "absolute", bottom: 2, right: 2, color: "error.main" }}
                                                    >
                                                        <DeleteOutlineOutlinedIcon />
                                                    </IconButton>
                                                    <Box
                                                        sx={{
                                                            cursor: "pointer",
                                                            p: 1.5,
                                                            textDecoration: "line-through",
                                                            color: "text.disabled",
                                                            whiteSpace: "pre-wrap",
                                                            wordBreak: "break-word",
                                                        }}
                                                    >
                                                        <Typography variant="h6">{task.name}</Typography>
                                                        <Typography variant="body2">{task.description}</Typography>
                                                    </Box>
                                                </CardContent>
                                            </Card>
                                        </motion.div>
                                    );
                                })}
                            </AnimatePresence>
                        </Stack>
                    </Box>
                </AccordionDetails>
            </Accordion>

            <CreateCategoryDialog
                open={open}
                onClose={handleClose}
                onCreate={handleCreate}
            />
        </Box>
    );
}
