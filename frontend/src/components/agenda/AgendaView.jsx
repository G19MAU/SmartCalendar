import {Box, Container, Typography, Paper, Button} from "@mui/material";
import CurrentDay from "./CurrentDay";
import NextDay from "./NextDay";
import {useCalendarContext} from "../../context/CalendarContext";
import AddActivity from "../calendar/AddActivity";
import ActivityDialog from "../calendar/ActivityDialog";
import ConfirmationDialog from "../ConfirmationDialog";
import IntroDialog from "../../components/IntroDialog";
import dayjs from "dayjs";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import {useState, useEffect, useRef} from "react";
import ToDoList from "../task/ToDoList";

function AgendaView() {
    const {
        /* activities */
        selectedActivity,
        setSelectedActivity,
        filteredActivities,
        weekdays,
        targetDate,
        timeSlots,
        setDay,
        currentYear,
        handleCloseDialog,
        handleClosePopover,
        dialogMode,
        confirmDeleteOpen,
        setConfirmDeleteOpen,
        openEditDialog,
        deleteActivity,
        isAddEditDialogOpen,
        anchorEl,
        placement,
        formData,
    } = useCalendarContext();

    const [startOfWeek, setStartOfWeek] = useState(
        dayjs().subtract(1, 'day')
    );

    const displayedWeekName = startOfWeek.add(1, 'day').week();
    const displayedYear = startOfWeek.add(1, 'day').year();

    const selectedDayIndex = startOfWeek.day();
    const selectedDayName = weekdays[selectedDayIndex].name;
    const currentDay = dayjs(startOfWeek).add(1, "day");
    const selectedDate = currentDay.format("DD/MM");

    const nextDayIndex = (selectedDayIndex + 1) % 7;
    const nextDayName = weekdays[nextDayIndex].name;
    const nextDay = currentDay.add(1, "day");
    const nextDate = nextDay.format("DD/MM");

    const [showIntroDialog, setShowIntroDialog] = useState(false);

    useEffect(() => {
        const seen = localStorage.getItem("introSeen");
        if (seen !== "true") {
            setShowIntroDialog(true);
        }
    }, []);

    const handleIntroClose = () => {
        localStorage.setItem("introSeen", "true");
        setShowIntroDialog(false);
    };

    const handlePrevDay = () => {
        setStartOfWeek(prev => dayjs(prev).subtract(1, 'day'));
    }
    const handleNextDay = () => {
        setStartOfWeek(prev => dayjs(prev).add(1, 'day'));
    }

    const [isPanelOpen, setIsPanelOpen] = useState(false);
    const panelRef = useRef(null);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (panelRef.current && !panelRef.current.contains(event.target)) {
                setIsPanelOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, []);


    return(
        <Box>
            <IntroDialog open={showIntroDialog} onClose={handleIntroClose} />
            <Box
                sx={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    mb: 1
                }}
            >
                {/* Button changing visible day to previous */}
                <Button variant="contained" size="small" onClick={handlePrevDay}>
                    <ArrowBackIcon fontSize="small"/>
                </Button>

                {/* Headtitle for agenda */}
                <Typography variant="h6">
                    Vecka {displayedWeekName} - {displayedYear}
                </Typography>

                {/* Button changing visible day to next */}
                <Button variant ="contained" size="small" onClick={handleNextDay}>
                    <ArrowForwardIcon fontSize="small"/>
                </Button>
            </Box>
            <Box sx={{ height: '73vh', display: 'flex', overflowY: 'hidden' }}>
                {/* LEFT COLUMN */}
                <Box sx={{ width: '40%', overflowY: 'auto', backgroundColor: '#fcfcfc', height:"510px", m:1, borderRadius:"3px" }}>
                    <CurrentDay startOfDay={startOfWeek} title={`${selectedDayName} ${selectedDate}`} />
                </Box>

                {/* RIGHT COLUMN */}
                <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>

                    <Box sx={{ height: '100vh', display: 'flex', overflowY: 'auto' }}>
                        <Box
                            sx={{
                                flex: 1,
                                mb:1,
                            }}
                        >
                            <ToDoList height={"520px"}/>
                        </Box>
                    </Box>

                    <Box
                        ref={panelRef}
                        sx={{
                            position: 'fixed',
                            bottom: 0,
                            right: 60,
                            width: 320,
                            maxHeight: isPanelOpen ? 400 : 30,
                            transition: 'max-height 0.3s ease',
                            overflow: 'hidden',
                            backgroundColor: '#fcfcfc',
                            borderTopLeftRadius: '12px',
                            borderTopRightRadius: '12px',
                            boxShadow: 3,
                            zIndex: 1300,
                        }}
                        onClick={() => setIsPanelOpen(true)}
                    >
                        <Box sx={{ p: 1, cursor: 'pointer', fontWeight: 'bold' , color: 'white', backgroundColor: 'primary.main'}}>
                            {nextDayName} {nextDate}
                        </Box>
                        <Box sx={{ p: 2, overflowY: 'auto', maxHeight: 340 }}>
                            <NextDay startOfDay={startOfWeek} title="" />
                        </Box>
                    </Box>

                </Box>
            </Box>



            {/* dialogs */}
            <AddActivity
                open={isAddEditDialogOpen}
                formData={formData}
                mode={dialogMode}
                onClose={handleCloseDialog}
            />

            <ActivityDialog
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                placement={placement}
                activity={selectedActivity}
                onClose={handleClosePopover}
                onEdit={() => {openEditDialog(selectedActivity)}}
                onDelete={() => setConfirmDeleteOpen(true)}
            />

            <ConfirmationDialog
                open={confirmDeleteOpen}
                title="Bekräfta borttagning"
                content="Är du säker på att du vill ta bort aktiviteten?"
                onClose={() => setConfirmDeleteOpen(false)}
                onConfirm={() => {
                    deleteActivity(selectedActivity.id);
                    setConfirmDeleteOpen(false);
                    setSelectedActivity(null);
                }}
            />
        </Box>
    )
}

export default AgendaView;
