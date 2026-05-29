package za.co.twc.togetherness.womens.club.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import za.co.twc.togetherness.womens.club.domain.Document;
import za.co.twc.togetherness.womens.club.repository.DocumentRepository;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document uploadForClaim(MultipartFile file, Long claimId, String documentType) throws IOException {
        validateFile(file);

        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setData(file.getBytes());
        document.setDocumentType(documentType);
        document.setClaimId(claimId);

        Document saved = documentRepository.save(document);
        LOGGER.info("Document '{}' uploaded for claim {}", saved.getFileName(), claimId);
        return saved;
    }

    public Document uploadForContribution(MultipartFile file, Long contributionId) throws IOException {
        validateFile(file);

        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setData(file.getBytes());
        document.setDocumentType("PROOF_OF_PAYMENT");
        document.setContributionId(contributionId);

        Document saved = documentRepository.save(document);
        LOGGER.info("Document '{}' uploaded for contribution {}", saved.getFileName(), contributionId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id " + id));
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsForClaim(Long claimId) {
        return documentRepository.findByClaimId(claimId);
    }

    @Transactional(readOnly = true)
    public List<Document> getDocumentsForContribution(Long contributionId) {
        return documentRepository.findByContributionId(contributionId);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum of 5MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed. Accepted: PDF, JPEG, PNG");
        }
    }
}
