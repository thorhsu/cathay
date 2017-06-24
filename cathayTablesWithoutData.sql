-- --------------------------------------------------------
-- Host:                         tpedbs001lv
-- Server version:               Microsoft SQL Server 2012 - 11.0.2100.60 (X64) 
-- Server OS:                    Windows NT 6.1 <X64> (Build 7601: Service Pack 1) (Hypervisor)
-- HeidiSQL Version:             8.3.0.4694
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES  */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping structure for table cathay.ActionHistory
CREATE TABLE IF NOT EXISTS "ActionHistory" (
	"id" INT NOT NULL,
	"userId" VARCHAR(20) NULL,
	"userName" NVARCHAR(50) NULL,
	"action" NVARCHAR(50) NULL,
	"actionTime" DATETIME NULL,
	"actionPageUrl" VARCHAR(100) NULL,
	"insertDate" DATETIME NULL,
	"updateDate" DATETIME NULL,
	"messaage" NVARCHAR(512) NULL,
	PRIMARY KEY ("id")
);

-- Data exporting was unselected.


-- Dumping structure for table cathay.afpFile
CREATE TABLE IF NOT EXISTS "afpFile" (
	"newBatchName" VARCHAR(20) NOT NULL,
	"serialNo" INT NULL,
	"centerSerialNo" INT NULL,
	"center" VARCHAR(3) NULL,
	"batchOrOnline" CHAR(1) NULL,
	"areaId" VARCHAR(10) NULL,
	"cycleDate" DATETIME NULL,
	"newBatchNo" INT NULL,
	"packIds" VARCHAR(2000) NULL,
	"sheets" INT NULL,
	"pages" INT NULL,
	"receipt" BIT NOT NULL,
	"ziped" BIT NOT NULL,
	"gpged" BIT NOT NULL,
	"status" NVARCHAR(30) NULL,
	"transfered" BIT NOT NULL,
	"vipModifierId" VARCHAR(20) NULL,
	"vipModifierName" VARCHAR(20) NULL,
	"vipSetTime" DATETIME NULL,
	"unziped" BIT NOT NULL,
	"afpFileNm" VARCHAR(25) NULL,
	"fileDate" DATETIME NULL,
	"insertDate" DATETIME NULL,
	"updateDate" DATETIME NULL,
	"presTime" DATETIME NULL,
	"printTime" DATETIME NULL,
	"bindTime" DATETIME NULL,
	"verifyTime" DATETIME NULL,
	"packTime" DATETIME NULL,
	"deliverTime" DATETIME NULL,
	"feedback" BIT NULL,
	"version" INT NULL,
	"beginTransferTime" DATETIME NULL,
	"endTransferTime" DATETIME NULL,
	PRIMARY KEY ("newBatchName")
);

-- Data exporting was unselected.


-- Dumping structure for table cathay.applyData
CREATE TABLE IF NOT EXISTS "applyData" (
	"cycleDate" DATETIME NULL,
	"processedDate" DATETIME NULL,
	"applyNo" VARCHAR(20) NULL,
	"policyNos" VARCHAR(256) NULL,
	"insureId" VARCHAR(35) NULL,
	"sourceCode" VARCHAR(10) NULL,
	"subAreaId" NVARCHAR(7) NULL,
	"areaAddress" NVARCHAR(200) NULL,
	"areaId" NVARCHAR(50) NULL,
	"receipt" BIT NULL,
	"areaName" NVARCHAR(50) NULL,
	"center" VARCHAR(2) NULL,
	"reprint" INT NULL,
	"merger" BIT NULL,
	"imageOk" BIT NULL,
	"docOk" BIT NULL,
	"megerOK" BIT NULL,
	"metaOk" BIT NULL,
	"signOk" BIT NULL,
	"printDate" DATETIME NULL,
	"totalPage" INT NULL,
	"firstPage" INT NULL,
	"a4Page" INT NULL,
	"dmPage" INT NULL,
	"lastPage" INT NULL,
	"s5Page" INT NULL,
	"s6Page" INT NULL,
	"s7Page" INT NULL,
	"s8Page" INT NULL,
	"afpBeginPage" INT NULL,
	"afpEndPage" INT NULL,
	"convertId" VARCHAR(10) NULL,
	"vip" BIT NULL,
	"substract" BIT NULL,
	"action2" CHAR(1) NULL,
	"action3" CHAR(1) NULL,
	"action4" CHAR(1) NULL,
	"oldBatchName" NVARCHAR(125) NOT NULL,
	"fileNm" NVARCHAR(100) NULL,
	"oldBatchNo" VARCHAR(8) NULL,
	"oldSerialNo" INT NULL,
	"newBatchName" VARCHAR(20) NULL,
	"newBatchNo" INT NULL,
	"newSerialNo" INT NULL,
	"presTime" DATETIME NULL,
	"printTime" DATETIME NULL,
	"bindTime" DATETIME NULL,
	"verifyTime" DATETIME NULL,
	"packTime" DATETIME NULL,
	"deliverTime" DATETIME NULL,
	"policyPDF" VARCHAR(150) NULL,
	"singPDF" VARCHAR(150) NULL,
	"exceptionStatus" VARCHAR(5) NULL,
	"policyStatus" VARCHAR(5) NULL,
	"verifyResult" VARCHAR(100) NULL,
	"productType" VARCHAR(10) NULL,
	"recName" NVARCHAR(512) NULL,
	"zip" VARCHAR(5) NULL,
	"packType" VARCHAR(10) NULL,
	"address" NVARCHAR(200) NULL,
	"channelID" VARCHAR(3) NULL,
	"channelName" NVARCHAR(50) NULL,
	"deliverType" CHAR(1) NULL,
	"uniqueNo" NVARCHAR(50) NULL,
	"mailType" NVARCHAR(20) NULL,
	"nonExistImgs" NVARCHAR(512) NULL,
	"insertDate" DATETIME NULL,
	"updateDate" DATETIME NULL,
	"vipModifierId" VARCHAR(20) NULL,
	"vipModifierName" VARCHAR(20) NULL,
	"vipModifierTime" DATETIME NULL,
	"substractModifiderId" VARCHAR(20) NULL,
	"substractModifiderName" VARCHAR(20) NULL,
	"substractModifiderTime" DATETIME NULL,
	"processedStaff" NVARCHAR(50) NULL,
	"processedTime" DATETIME NULL,
	"packId" NVARCHAR(20) NULL,
	"version" INT NULL,
	"reSendStaff" VARCHAR(30) NULL,
	"reSendTime" DATETIME NULL,
	"serviceCenter" NVARCHAR(50) NULL,
	"serviceCenterNm" NVARCHAR(100) NULL,
	"beginTransferTime" DATETIME NULL,
	"endTransferTime" DATETIME NULL,
	"groupInsure" BIT NULL,
	"haveInsureCard" BIT NULL,
	"receiver" NVARCHAR(50) NULL,
	"receiverBank" NVARCHAR(128) NULL,
	"bankReceiptId" NVARCHAR(128) NULL,
	"parseNorm" BIT NULL,
	"pareseString" NVARCHAR(256) NULL,
	"agentNm" NVARCHAR(50) NULL,
	"addressEq" BIT NULL,
	"havaBkReceipt" BIT NULL,
	"cd" BIT NULL,
	"bkReceiptMatched" BIT NULL,
	"weight" DECIMAL NULL,
	"mailReceiptIndex" NVARCHAR(5) NULL,
	PRIMARY KEY ("oldBatchName")
);

-- Data exporting was unselected.


-- Dumping structure for table cathay.bankReceipt
CREATE TABLE IF NOT EXISTS "bankReceipt" (
	"bankReceiptId" NVARCHAR(30) NOT NULL,
	"matchDate" DATETIME NULL,
	"matchUser" VARCHAR(20) NULL,
	"packDate" DATETIME NULL,
	"packUser" VARCHAR(20) NULL,
	"oldBatchName" NVARCHAR(125) NULL,
	"insertDate" DATETIME NULL,
	"issueDate" DATETIME NULL,
	"issueUser" VARCHAR(20) NULL,
	"status" NVARCHAR(128) NULL,
	"exceptionStatus" NVARCHAR(128) NULL,
	"receiveDate" DATETIME NULL,
	"receiveUser" NVARCHAR(20) NULL,
	"dateSerialNo" INT NULL,
	"receiveTime" DATETIME NULL,
	"center" NVARCHAR(10) NULL,
	"dateCenterSerialNo" INT NULL,
	"fxBackReceiver" NVARCHAR(50) NULL,
	"fxBackReceiveDate" DATETIME NULL,
	PRIMARY KEY ("bankReceiptId")
);

-- Data exporting was unselected.


-- Dumping structure for table cathay.errorReport
CREATE TABLE IF NOT EXISTS "errorReport" (
	"id" INT NOT NULL,
	"title" NVARCHAR(256) NULL,
	"errorType" NVARCHAR(100) NULL,
	"errHappenTime" DATETIME NULL,
	"messageBody" NVARCHAR(1024) NULL,
	"reported" BIT NOT NULL,
	"oldBatchName" NVARCHAR(512) NULL,
	"reportTime" DATETIME NULL,
	"version" INT NULL,
	"exception" BIT NULL,
	PRIMARY KEY ("id")
);

-- Data exporting was unselected.


-- Dumping structure for table cathay.imgMetaTable
CREATE TABLE IF NOT EXISTS "imgMetaTable" (
	"imgId" INT NOT NULL,
	"oldBatchName" NVARCHAR(125) NOT NULL,
	PRIMARY KEY ("imgId","oldBatchName")
);

-- Data exporting was unselected.


-- Dumping structure for table cathay.logisticStatus
CREATE TABLE IF NOT EXISTS "logisticStatus" (
	"logisticId" VARCHAR(15) NOT NULL,
	"scanDate" DATETIME NULL,
	"books" INT NULL,
	"receipts" INT NULL,
	"packs" INT NULL,
	"cycleDate" DATETIME NULL,
	"address" NVARCHAR(500) NULL,
	"packDone" BIT NULL,
	"firstUniqueNo" NVARCHAR(20) NULL,
	"center" VARCHAR(5) NULL,
	"vendorId" VARCHAR(50) NULL,
	"name" NVARCHAR(1000) NULL,
	"tel" NVARCHAR(1000) NULL,
	"sentTime" DATETIME NULL,
	"mailReceipt" BIT NULL,
	"batchOrOnline" CHAR(1) NULL,
	"weight" DECIMAL NULL,
	PRIMARY KEY ("logisticId")
);

-- Data exporting was unselected.


-- Dumping structure for table cathay.packStatus
CREATE TABLE IF NOT EXISTS "packStatus" (
	"packId" NVARCHAR(20) NOT NULL,
	"cycleDate" DATETIME NOT NULL,
	"subAreaId" NVARCHAR(12) NOT NULL,
	"subAreaName" NVARCHAR(256) NULL,
	"subAreaTel" NVARCHAR(20) NULL,
	"areaAddress" NVARCHAR(150) NULL,
	"center" VARCHAR(5) NULL,
	"status" INT NOT NULL,
	"statusNm" NVARCHAR(10) NULL,
	"createDate" DATETIME NOT NULL,
	"back" BIT NOT NULL,
	"books" INT NULL,
	"receipts" INT NULL,
	"updateDate" DATETIME NULL,
	"policyScanDate" DATETIME NULL,
	"receiptScanDate" DATETIME NULL,
	"labelScanDate" DATETIME NULL,
	"newBatchNms" NVARCHAR(2000) NULL,
	"policyScanUser" VARCHAR(20) NULL,
	"receiptScanUser" VARCHAR(20) NULL,
	"labelScanUser" VARCHAR(20) NULL,
	"firstUniqueNo" NVARCHAR(50) NULL,
	"logisticId" NVARCHAR(15) NULL,
	"packCompleted" BIT NULL,
	"reported" BIT NULL,
	"inusreCard" INT NULL,
	"batchOrOnline" CHAR(1) NULL,
	"zipCode" VARCHAR(20) NULL,
	"serviceCenterNm" NVARCHAR(70) NULL,
	"weight" DECIMAL NULL,
	PRIMARY KEY ("packId")
);

-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
