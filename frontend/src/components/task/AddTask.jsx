import React, { useState } from "react";
import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    TextField,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Divider,
} from "@mui/material";
import { useCategoryContext } from "../../context/CategoryContext";
import CreateCategoryDialog from "../CreateCategoryDialog";

const AddTask = ({ open, onClose, formData, handleChange, handleSubmit}) => {
    const { categories, createCategory } = useCategoryContext();
    const [openCreateCategoryDialog, setOpenCreateCategoryDialog] = useState(false);

    const onCategoryChange = (event) => {
        const value = event.target.value;
        // If they picked the “new” trigger, open the dialog instead of selecting a real ID:
        if (value === "__CREATE_NEW__") {
            setOpenCreateCategoryDialog(true);
        } else {
            handleChange(event);
        }
    };

    // After creating, close the dialog
    const handleCategoryCreated = async (newCat) => {
        const createdCat = await createCategory(newCat.name, newCat.color);
        console.log("created cat: ", createdCat);
        setOpenCreateCategoryDialog(false);
        handleChange({
            target: {
                name: "categoryId",
                value: createdCat.id,
            },
        });
    };

    return(
        <>
        <Dialog open={open} onClose={onClose}>
            <DialogTitle m={1}>Lägg till en ToDo</DialogTitle>
            <DialogContent sx={{width:"400px", ml:1}}>
                <Box mb ={2}>
                    <TextField
                        name="name"
                        label="Titel"
                        value={formData.name}
                        onChange={handleChange}
                        required
                    />
                </Box>
                <Box mb ={2}>
                    <TextField
                        name="description"
                        label="Beskrivning"
                        value={formData.description}
                        onChange={handleChange}
                    />
                </Box>
                <Box mb ={2}>
                    <TextField
                        name="location"
                        label="Plats"
                        value={formData.location}
                        onChange={handleChange}
                    />
                </Box>
                {/*Category selection*/}
                <Box mb={2}>
                    <FormControl fullWidth>
                        <InputLabel id="task-category-label">Kategori</InputLabel>
                        <Select
                            labelId="task-category-label"
                            name="categoryId"
                            value={formData.categoryId}
                            label="Kategori"
                            onChange={onCategoryChange}
                        >
                            <MenuItem value="">
                                <em>Ingen</em>
                            </MenuItem>
                            {categories.map((cat) => (
                                <MenuItem key={cat.id} value={cat.id}>
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

                            {/* “Create new” trigger */}
                            <MenuItem value="__CREATE_NEW__">
                                <Box sx={{textAlign: "center", width: "100%" }}>
                                    Skapa ny kategori
                                </Box>
                            </MenuItem>

                        </Select>
                    </FormControl>
                </Box>
                <Box mb ={2}>
                    <TextField
                        name="date"
                        label="Datum"
                        type="date"
                        InputLabelProps={{shrink: true}}
                        value={formData.date}
                        onChange={handleChange}
                    />
                </Box>
            </DialogContent>

            <DialogActions>
                <Button onClick = {onClose}>Avbryt</Button>
                <Button
                    onClick = {handleSubmit}
                    variant="contained"
                    disabled={!formData.name.trim()
                }>
                    Spara
                </Button>
            </DialogActions>
        </Dialog>

            {/* The shared CreateCategoryDialog */}
            <CreateCategoryDialog
                open={openCreateCategoryDialog}
                onClose={() => setOpenCreateCategoryDialog(false)}
                onCreate={handleCategoryCreated}
            />
        </>
    );
};

export default AddTask;