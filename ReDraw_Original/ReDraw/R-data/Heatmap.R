# This script uses gplots package

# setwd(".")
setwd("/Users/semeru/git/gitlab-semeru/ReDraw/R-data")

dataFiles <- 2

for (i in 1:dataFiles) {

  all.data <- read.csv(paste("distribution",i,".csv",sep = ""))  # load the data
  #dim(all.data)
  row.names(all.data) <- all.data$category
  all.rows <- all.data$category
  all.data2 <- all.data[, -1]
  
  # Data smoothing
  all.data2 <- sqrt(sqrt(all.data2))
  # all.data2 <- sqrt(all.data2)
  # Change NaN for 0
  all.data2[is.na(all.data2)] <- 0
  
  # data.prop <- all.data2/rowSums(all.data2)
  # data.prop <- all.data2/colSums(all.data2)
  x <- as.matrix(all.data2)
  
  
  # color <- colorRampPalette(c("lightyellow", "red"), space = "rgb")(100)
  # This palette is so much better, this also works for gray scale and 
  color <- colorRampPalette(c(viridis::viridis(100)), space = "rgb")
  
  
  # Generate pdf
  pdf(file=paste("distribution",i,".pdf",sep = "")) 
  
  # Plot heatmap with heatmap.2
  par(cex.main=0.75) # Shrink title fonts on plot
  # Margins outside the plot
  par(oma=c(3,0,0,4))
  # heatmap.2(as.matrix(all.data2), Colv = NULL, Rowv = NULL, dendrogram = "row", # Tidy, normalised data
  
  heatmap.2(x, ## matrix with the data
            main="Heatmap Components by Category",
            dendrogram = "none", ##  no dendrogram plotted, but reordering done.
            density="density",
            margins = c(6,7.2), ## margins for the plot
  
            offsetRow = -0.1, ##  offset for text on rows
            offsetCol = -0.1, ##  offset for text on columns
            ylab = "Google Play Categories",
            xlab = "GUI Components",
            trace="none", # Turn off trace lines from heat map
            col = color,           # color scheme
            cexRow=0.5, cexCol=0.75)     # Amend row and column label fonts
  dev.off()
}
